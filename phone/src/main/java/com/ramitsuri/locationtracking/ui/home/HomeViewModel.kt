package com.ramitsuri.locationtracking.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.geofence.AndroidGeofenceManager
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LocationsViewMode
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.permissions.PermissionResult
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.ui.home.HomeViewState.ViewMode.LocationsForDate
import com.ramitsuri.locationtracking.ui.region.toLatLng
import com.ramitsuri.locationtracking.utils.combine
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val locationRepository: LocationRepository,
    permissionState: () -> StateFlow<List<PermissionResult>>,
    isUploadWorkerRunning: () -> Flow<Boolean>,
    private val upload: () -> Unit,
    private val timeZone: TimeZone,
    private val settings: Settings,
    private val regionDao: RegionDao,
    private val locationProvider: LocationProvider,
    private val geofenceManager: AndroidGeofenceManager,
) : ViewModel() {
    private val viewMode =
        MutableStateFlow<HomeViewState.ViewMode>(HomeViewState.ViewMode.LastKnownLocation())
    private val isLoading = MutableStateFlow(false)
    private val selectedLocation = MutableStateFlow<Location?>(null)
    private val userMessage = MutableStateFlow<String?>(null)

    val viewState = combine(
        locationRepository.getCount(),
        permissionState(),
        isUploadWorkerRunning(),
        locationRepository.getLastKnownLocation(),
        viewMode,
        isLoading,
        selectedLocation,
        regionDao.getAllFlow(),
        userMessage,
    ) {
            count, permissions, isUploadWorkerRunning, lastKnownLocation, viewMode, isLoading,
            selectedLocation, regions, userMessage,
        ->
        val newViewMode = when (viewMode) {
            is LocationsForDate -> viewMode
            is HomeViewState.ViewMode.LastKnownLocation -> {
                viewMode.copy(lastKnownLocation)
            }
        }
        HomeViewState(
            numOfLocations = count,
            permissionsResults = permissions,
            isUploadWorkerRunning = isUploadWorkerRunning,
            viewMode = newViewMode,
            isLoading = isLoading,
            selectedLocation = selectedLocation,
            regions = regions,
            userMessage = userMessage,
            timeZone = timeZone,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeViewState(timeZone = timeZone),
    )

    fun onUpload() {
        upload()
    }

    fun dateTimeSelectedForLocations(
        fromDateTime: LocalDateTime,
        toDateTime: LocalDateTime?,
    ) {
        val from = fromDateTime.toInstant(timeZone)
        val to = toDateTime?.toInstant(timeZone) ?: from.plus(1.days).minus(1.nanoseconds)
        clearSelectedLocation()
        isLoading.value = true
        viewModelScope.launch {
            locationRepository.get(
                from = from,
                to = to,
                minAccuracyMeters = settings.getMinAccuracyForDisplay().first(),
            ).let { locations ->
                val fromLocal = from.toLocalDateTime(timeZone)
                val toLocal = to.toLocalDateTime(timeZone)
                viewMode.value = LocationsForDate(
                    fromDate = fromLocal.date,
                    fromTime = fromLocal.time,
                    toDate = toLocal.date,
                    toTime = toLocal.time,
                    locations = locations,
                    mode = settings.getLocationsViewMode().first(),
                    timeline = createWifiTimeline(locations)
                )
            }
            isLoading.value = false
        }
    }

    fun clearDateForLocations() {
        clearSelectedLocation()
        viewMode.update {
            HomeViewState.ViewMode.LastKnownLocation()
        }
    }

    fun selectLocation(location: Location) {
        selectedLocation.value = location
    }

    fun clearSelectedLocation() {
        selectedLocation.value = null
    }

    fun setLocationsViewMode(mode: LocationsViewMode) {
        viewModelScope.launch {
            settings.setLocationsViewMode(mode)
        }
        viewMode.update {
            when (it) {
                is LocationsForDate -> it.copy(mode = mode)
                else -> it
            }
        }
    }

    fun onUserMessageShown() {
        userMessage.value = null
    }

    fun onAddTempGeofence() {
        viewModelScope.launch {
            isLoading.value = true
            val freshLocation = getAccurateLocationFix(maxAttempts = 3, targetAccuracyMeters = 50)
            if (freshLocation == null) {
                logW(TAG) { "Failed to get accurate location fix for temporary geofence" }
                isLoading.value = false
                return@launch
            }

            val tempName = generateTempRegionName()

            val tempRegion = Region(
                name = tempName,
                center = freshLocation.toLatLng(),
                radiusMeters = Region.DEFAULT_RADIUS_METERS,
                type = Region.Type.TEMP,
            )

            regionDao.insert(tempRegion)
            geofenceManager.syncGeofences()
            logI(TAG) { "Created temporary geofence '$tempName' at ($freshLocation)" }

            userMessage.value = tempName
            isLoading.value = false
        }
    }

    private suspend fun getAccurateLocationFix(
        maxAttempts: Int = 3,
        targetAccuracyMeters: Int = 50,
    ): Location? {
        var bestFix: Location? = null
        for (attempt in 1..maxAttempts) {
            val fix = locationProvider.requestSingle()
            if (fix != null) {
                if (bestFix == null || fix.accuracy < bestFix.accuracy) {
                    bestFix = fix
                }
                if (fix.accuracy <= targetAccuracyMeters) {
                    return fix
                }
            }
            if (attempt < maxAttempts) {
                delay(1.seconds)
            }
        }
        return bestFix
    }

    @Suppress("DEPRECATION")
    private fun generateTempRegionName(): String {
        val now = Clock.System.now().toLocalDateTime(timeZone)
        val year = now.year.toString().padStart(4, '0')
        val month = now.monthNumber.toString().padStart(2, '0')
        val day = now.dayOfMonth.toString().padStart(2, '0')
        val hour = now.hour.toString().padStart(2, '0')
        val minute = now.minute.toString().padStart(2, '0')
        val second = now.second.toString().padStart(2, '0')
        return "temp_${year}${month}${day}_${hour}${minute}${second}"
    }

    private fun createWifiTimeline(locations: List<Location>): List<LocationsForDate.Event> {
        if (locations.isEmpty()) return emptyList()

        val events = mutableListOf<LocationsForDate.Event>()
        var currentConnectedWifi: String? = null

        locations.forEachIndexed { index, location ->
            val ssid = location.ssid
            val time = location.locationTimestamp.toLocalDateTime(timeZone)

            if (index == 0) {
                if (ssid != null) {
                    events.add(
                        LocationsForDate.Event(
                            type = LocationsForDate.Event.Type.START,
                            time = time,
                            wifiName = ssid,
                        )
                    )
                    currentConnectedWifi = ssid
                }
            } else {
                if (currentConnectedWifi != null && ssid == null) {
                    events.add(
                        LocationsForDate.Event(
                            type = LocationsForDate.Event.Type.DISCONNECTED,
                            time = time,
                            wifiName = currentConnectedWifi,
                        )
                    )
                    currentConnectedWifi = null
                } else if (currentConnectedWifi == null && ssid != null) {
                    events.add(
                        LocationsForDate.Event(
                            type = LocationsForDate.Event.Type.CONNECTED,
                            time = time,
                            wifiName = ssid,
                        )
                    )
                    currentConnectedWifi = ssid
                } else if (currentConnectedWifi != null && ssid != null && currentConnectedWifi != ssid) {
                    events.add(
                        LocationsForDate.Event(
                            type = LocationsForDate.Event.Type.DISCONNECTED,
                            time = time,
                            wifiName = currentConnectedWifi,
                        )
                    )
                    events.add(
                        LocationsForDate.Event(
                            type = LocationsForDate.Event.Type.CONNECTED,
                            time = time,
                            wifiName = ssid,
                        )
                    )
                    currentConnectedWifi = ssid
                }
            }
        }

        return events
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
