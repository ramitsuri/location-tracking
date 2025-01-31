package com.ramitsuri.locationtracking.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.permissions.PermissionResult
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.utils.combine
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class HomeViewModel(
    private val locationRepository: LocationRepository,
    permissionState: () -> StateFlow<List<PermissionResult>>,
    isUploadWorkerRunning: () -> Flow<Boolean>,
    private val upload: () -> Unit,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val viewMode =
        MutableStateFlow<HomeViewState.ViewMode>(HomeViewState.ViewMode.LastKnownLocation())
    private val isLoading = MutableStateFlow(false)
    private val selectedLocation = MutableStateFlow<Location?>(null)

    val viewState = combine(
        locationRepository.getCount(),
        permissionState().map { results ->
            results.filter { permissionResult ->
                !permissionResult.granted
            }.map { it.permission }
        },
        isUploadWorkerRunning(),
        locationRepository.getLastKnownLocation(),
        viewMode,
        isLoading,
        selectedLocation,
    ) { count, permissions, isUploadWorkerRunning, lastKnownLocation, viewMode, isLoading,
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
            permissionsNotGranted = permissions.isNotEmpty(),
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

    fun dateSelectedForLocations(date: LocalDate) {
        val from = date.atStartOfDayIn(timeZone)
        val to = from.plus(1.days).minus(1.nanoseconds)
        clearSelectedLocation()
        isLoading.value = true
        viewModelScope.launch {
            locationRepository.get(
                from = from,
                to = to,
            ).let { locations ->
                viewMode.update {
                    HomeViewState.ViewMode.LocationsForDate(
                        date = date,
                        locations = locations,
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
}
