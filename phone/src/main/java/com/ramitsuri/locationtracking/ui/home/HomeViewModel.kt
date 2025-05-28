package com.ramitsuri.locationtracking.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LocationsViewMode
import com.ramitsuri.locationtracking.permissions.PermissionResult
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.utils.combine
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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds

class HomeViewModel(
    private val locationRepository: LocationRepository,
    permissionState: () -> StateFlow<List<PermissionResult>>,
    isUploadWorkerRunning: () -> Flow<Boolean>,
    private val upload: () -> Unit,
    private val timeZone: TimeZone,
    private val settings: Settings,
) : ViewModel() {
    private val viewMode =
        MutableStateFlow<HomeViewState.ViewMode>(HomeViewState.ViewMode.LastKnownLocation())
    private val isLoading = MutableStateFlow(false)
    private val selectedLocation = MutableStateFlow<Location?>(null)

    val viewState = combine(
        locationRepository.getCount(),
        permissionState(),
        isUploadWorkerRunning(),
        locationRepository.getLastKnownLocation(),
        viewMode,
        isLoading,
        selectedLocation,
    ) {
            count, permissions, isUploadWorkerRunning, lastKnownLocation, viewMode, isLoading,
            selectedLocation,
        ->
        val newViewMode = when (viewMode) {
            is HomeViewState.ViewMode.LocationsForDate -> viewMode
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
                viewMode.update {
                    val fromLocal = from.toLocalDateTime(timeZone)
                    val toLocal = to.toLocalDateTime(timeZone)
                    HomeViewState.ViewMode.LocationsForDate(
                        fromDate = fromLocal.date,
                        fromTime = fromLocal.time,
                        toDate = toLocal.date,
                        toTime = toLocal.time,
                        locations = locations,
                        mode = settings.getLocationsViewMode().first(),
                    )
                }
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
                is HomeViewState.ViewMode.LocationsForDate -> it.copy(mode = mode)
                else -> it
            }
        }
    }
}
