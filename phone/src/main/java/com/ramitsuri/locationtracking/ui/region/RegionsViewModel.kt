package com.ramitsuri.locationtracking.ui.region

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.data.dao.LogItemDao
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.geofence.AndroidGeofenceManager
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.repository.LocationRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class RegionsViewModel(
    private val regionDao: RegionDao,
    logItemDao: LogItemDao,
    locationRepository: LocationRepository,
    timeZone: TimeZone,
    private val geofenceManager: AndroidGeofenceManager? = null,
) : ViewModel() {
    private val unsavedRegion = MutableStateFlow(Region.EMPTY)

    val viewState = combine(
        regionDao.getAllFlow(),
        unsavedRegion,
        logItemDao.getGeofenceLogsFlow(Clock.System.now().minus(3.days)),
    ) { regions, unsavedRegion, geofenceLogs ->
        RegionsViewState(
            regions = regions,
            unsavedRegion = unsavedRegion,
            geofenceLogs = geofenceLogs,
            initialMapCenter = locationRepository.getLastKnownLocation().firstOrNull()?.toLatLng()
                ?: LatLng.NY,
            timeZone = timeZone,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RegionsViewState(timeZone = timeZone),
    )

    fun onRegionLatLng(latLng: AndroidLatLng) {
        val newPoint = latLng.toLatLng()
        unsavedRegion.value = unsavedRegion.value.copy(
            latLngs = unsavedRegion.value.latLngs + newPoint,
        )
    }

    fun onRegionNameChanged(name: String) {
        unsavedRegion.value = unsavedRegion.value.copy(name = name)
    }

    fun onClearUnsavedRegion() {
        unsavedRegion.value = Region.EMPTY
    }

    fun onSaveRegion() {
        viewModelScope.launch {
            val state = viewState.value
            if (!state.canSaveRegion) {
                logI(TAG) { "Cannot save region" }
                return@launch
            }
            regionDao.insert(state.unsavedRegion)
            unsavedRegion.value = Region.EMPTY
            geofenceManager?.syncGeofences()
        }
    }

    fun onDeleteRegion(region: Region) {
        viewModelScope.launch {
            regionDao.delete(region)
            geofenceManager?.syncGeofences()
        }
    }

    companion object {
        private const val TAG = "RegionViewModel"
    }
}
