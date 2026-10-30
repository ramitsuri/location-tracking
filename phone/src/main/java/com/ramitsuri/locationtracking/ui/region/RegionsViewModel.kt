package com.ramitsuri.locationtracking.ui.region

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.data.dao.GeofenceEventDao
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class RegionsViewModel(
    private val regionDao: RegionDao,
    geofenceEventDao: GeofenceEventDao,
    locationRepository: LocationRepository,
    timeZone: TimeZone,
    private val geofenceManager: AndroidGeofenceManager? = null,
) : ViewModel() {
    private val unsavedRegion = MutableStateFlow<UnsavedRegion?>(null)

    val viewState = combine(
        regionDao.getAllFlow(),
        unsavedRegion,
        geofenceEventDao.getEventsFlow(Clock.System.now().minus(3.days)),
        locationRepository.getLastKnownLocation(),
    ) { regions, unsavedRegion, geofenceEvents, lastKnownLocation ->
        RegionsViewState(
            regions = regions,
            unsavedRegion = unsavedRegion,
            geofenceEvents = geofenceEvents,
            lastKnownLocation = lastKnownLocation,
            initialMapCenter = lastKnownLocation?.toLatLng()
                ?: LatLng.NY,
            timeZone = timeZone,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RegionsViewState(timeZone = timeZone),
    )

    fun onRegionLatLng(latLng: AndroidLatLng) {
        val center = latLng.toLatLng()
        unsavedRegion.value = unsavedRegion.value?.copy(center = center)
            ?: UnsavedRegion(center = center)
    }

    fun onRegionRadiusChanged(radiusMeters: Float) {
        unsavedRegion.value = unsavedRegion.value?.copy(
            radiusMeters = radiusMeters.coerceIn(
                Region.MIN_RADIUS_METERS,
                Region.MAX_RADIUS_METERS,
            ),
        )
    }

    fun onRegionNameChanged(name: String) {
        unsavedRegion.value = unsavedRegion.value?.copy(name = name)
    }

    fun onClearUnsavedRegion() {
        unsavedRegion.value = null
    }

    fun onSaveRegion() {
        viewModelScope.launch {
            val state = viewState.value
            if (!state.canSaveRegion) {
                logI(TAG) { "Cannot save region" }
                return@launch
            }
            val unsaved = state.unsavedRegion ?: return@launch
            regionDao.insert(
                Region(
                    name = unsaved.name.trim(),
                    center = unsaved.center,
                    radiusMeters = unsaved.radiusMeters,
                ),
            )
            unsavedRegion.value = null
            geofenceManager?.syncGeofences()
        }
    }

    fun onDeleteRegion(region: Region) {
        viewModelScope.launch {
            regionDao.delete(region)
            geofenceManager?.syncGeofences()
        }
    }

    fun onUpdateRegion(region: Region) {
        viewModelScope.launch {
            val updated = region.copy(
                name = region.name.trim(),
                radiusMeters = region.radiusMeters.coerceIn(
                    Region.MIN_RADIUS_METERS,
                    Region.MAX_RADIUS_METERS,
                ),
            )
            regionDao.insert(updated)
            geofenceManager?.syncGeofences()
        }
    }

    companion object {
        private const val TAG = "RegionViewModel"
    }
}
