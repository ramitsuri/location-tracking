package com.ramitsuri.locationtracking.ui.region

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegionsViewModel(
    private val regionDao: RegionDao,
    locationRepository: LocationRepository,
) : ViewModel() {
    private val unsavedRegion = MutableStateFlow(Region.EMPTY)

    val viewState = combine(
        regionDao.getAllFlow(),
        unsavedRegion,
    ) { regions, unsavedRegion,
        ->
        RegionsViewState(
            regions = regions,
            unsavedRegion = unsavedRegion,
            initialMapCenter = locationRepository.getLastKnownLocation().firstOrNull()?.toLatLng()
                ?: LatLng.NY,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RegionsViewState(),
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
        }
    }

    fun onDeleteRegion(region: Region) {
        viewModelScope.launch {
            regionDao.delete(region)
        }
    }

    companion object {
        private const val TAG = "RegionViewModel"
    }
}
