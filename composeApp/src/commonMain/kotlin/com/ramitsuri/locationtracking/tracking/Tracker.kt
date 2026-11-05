package com.ramitsuri.locationtracking.tracking

import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.battery.BatteryInfoProvider
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.location.Request
import com.ramitsuri.locationtracking.tracking.location.forMonitoringMode
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import com.ramitsuri.locationtracking.utils.RegionUtil
import com.ramitsuri.locationtracking.utils.toBd
import java.math.BigDecimal
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Tracker(
    private val permissionChecker: PermissionChecker,
    private val locationProvider: LocationProvider,
    private val wifiInfoProvider: WifiInfoProvider,
    private val batteryInfoProvider: BatteryInfoProvider,
    private val locationRepository: LocationRepository,
    private val geocoderRepository: GeocoderRepository,
    private val settings: Settings,
    private val scope: CoroutineScope,
    private val regionDao: RegionDao,
    private val regionUtil: RegionUtil,
) {

    private var locationCollectionJob: Job? = null
    private var reverseGeocodeJob: Job? = null
    private var saveLastKnownLocationJob: Job? = null

    private val _lastKnownAddressOrLocation: MutableStateFlow<LocationAndAddress?> =
        MutableStateFlow(null)
    val lastKnownAddressOrLocation = _lastKnownAddressOrLocation.asStateFlow()

    fun startTracking() {
        logI(TAG) { "startTracking" }
        wifiInfoProvider.requestUpdates()
        setupLocationRequest()
    }

    fun stopTracking() {
        wifiInfoProvider.unrequestUpdates()
        locationCollectionJob?.cancel()
    }

    fun trackSingle() {
        scope.launch {
            if (!permissionChecker.hasPermissions(
                    listOf(
                        Permission.FINE_LOCATION,
                        Permission.COARSE_LOCATION,
                    ),
                ).any { it.granted }
            ) {
                return@launch
            }
            locationProvider.requestSingle()?.let { location ->
                val wifiInfo = wifiInfoProvider.wifiInfo.value
                location.copy(
                    ssid = wifiInfo.ssid,
                    bssid = wifiInfo.bssid,
                    battery = batteryInfoProvider.getLevel(),
                    batteryStatus = batteryInfoProvider.getChargingStatus(),
                    inRegions = listOf(),
                ).let {
                    locationRepository.insert(it)
                    onNewLocation(it)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupLocationRequest() {
        logI(TAG) { "setupLocationRequest" }
        locationCollectionJob?.cancel()
        if (!permissionChecker.hasPermissions(
                listOf(
                    Permission.FINE_LOCATION,
                    Permission.COARSE_LOCATION,
                ),
            ).any { it.granted }
        ) {
            logW(TAG) { "No permissions, cannot setup tracking" }
            return
        }
        locationCollectionJob = scope.launch {
            settings
                .getMonitoringMode()
                .flatMapLatest { mode ->
                    Request.forMonitoringMode(mode)
                        ?.let { locationRequest ->
                            locationProvider.requestUpdates(locationRequest)
                                .mapNotNull { location ->
                                    if (location.accuracy > MIN_LOCATION_ACCURACY_METERS) {
                                        logI(TAG) { "Ignoring because inaccurate $location" }
                                        null
                                    } else {
                                        val wifiInfo = wifiInfoProvider.wifiInfo.value
                                        location.copy(
                                            ssid = wifiInfo.ssid,
                                            bssid = wifiInfo.bssid,
                                            battery = batteryInfoProvider.getLevel(),
                                            batteryStatus = batteryInfoProvider.getChargingStatus(),
                                            monitoringMode = mode,
                                        )
                                    }
                                }
                        } ?: emptyFlow()
                }.collect { location ->
                    handleLocation(location)
                }
        }
    }

    private suspend fun handleLocation(location: Location) {
        if (location.monitoringMode == MonitoringMode.TempRegion) {
            val tempRegions = regionDao.getAll().filter { it.type == Region.Type.TEMP }
            if (tempRegions.isEmpty()) {
                logI(TAG) { "In temp region mode, but no temp regions found. Switching to Move" }
                withContext(NonCancellable) {
                    val updatedLocation = location.copy(monitoringMode = MonitoringMode.Move)
                    locationRepository.insert(updatedLocation)
                    settings.setMonitoringMode(MonitoringMode.Move)
                    onNewLocation(updatedLocation)
                }
                return
            }
            val isInTempRegion = tempRegions.any { regionUtil.contains(it, location) }
            if (isInTempRegion) {
                logD(TAG) { "Inside temp region, skipping location recording" }
                return
            }
            if (location.accuracy > MAX_TEMP_REGION_EXIT_ACCURACY_METERS) {
                logI(TAG) { "Outside temp region but accuracy is poor (${location.accuracy}m), ignoring" }
                return
            }
            logI(TAG) { "Moved outside temp region. Deleting temp region(s) and switching to Move" }
            withContext(NonCancellable) {
                tempRegions.forEach { regionDao.delete(it) }
                val updatedLocation = location.copy(monitoringMode = MonitoringMode.Move)
                locationRepository.insert(updatedLocation)
                settings.setMonitoringMode(MonitoringMode.Move)
                onNewLocation(updatedLocation)
            }
            return
        }

        locationRepository.insert(location)
        onNewLocation(location)
    }

    private fun onNewLocation(location: Location) {
        logD(TAG) { "onNewLocation: $location" }
        saveLastKnownLocationJob?.cancel()
        saveLastKnownLocationJob = scope.launch {
            delay(1.seconds)
            settings.setLastKnownLocation(location)
        }
        var shouldReverseGeocode = true
        // Put coordinates right away while we wait to get address so that we're able to show
        // something to the user indicating the location has changed
        _lastKnownAddressOrLocation.update { existing ->
            if (existing == null || !existing.isSameLocation(location)) {
                LocationAndAddress(location.latitude, location.longitude)
            } else {
                shouldReverseGeocode = false
                existing
            }
        }
        if (!shouldReverseGeocode) {
            return
        }
        reverseGeocodeJob?.cancel()
        reverseGeocodeJob = scope.launch {
            delay(500)
            geocoderRepository.reverseGeocode(
                location.latitude,
                location.longitude,
            )?.let { address ->
                _lastKnownAddressOrLocation.update {
                    LocationAndAddress(
                        location.latitude,
                        location.longitude,
                        address,
                    )
                }
            }
        }
    }

    data class LocationAndAddress(
        val lat: BigDecimal,
        val lon: BigDecimal,
        val address: String? = null,
    ) {
        constructor(lat: Double, lon: Double) : this(
            lat.toBd(),
            lon.toBd(),
        )

        constructor(lat: Double, lon: Double, address: String) : this(
            lat.toBd(),
            lon.toBd(),
            address,
        )

        fun isSameLocation(location: Location): Boolean {
            return lat.compareTo(location.latitude.toBd()) == 0 &&
                lon.compareTo(location.longitude.toBd()) == 0
        }

        fun string(): String = address ?: "$lat, $lon"
    }

    companion object {
        private const val TAG = "Tracker"
        private const val MIN_LOCATION_ACCURACY_METERS = 500
        private const val MAX_TEMP_REGION_EXIT_ACCURACY_METERS = 100
    }
}
