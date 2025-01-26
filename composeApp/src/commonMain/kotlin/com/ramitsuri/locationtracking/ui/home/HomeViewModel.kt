package com.ramitsuri.locationtracking.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.permissions.PermissionResult
import com.ramitsuri.locationtracking.repository.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    locationRepository: LocationRepository,
    permissionState: () -> StateFlow<List<PermissionResult>>,
) : ViewModel() {
    val viewState = combine(
        locationRepository.getCount(),
        permissionState().map { results ->
            results.filter { permissionResult ->
                !permissionResult.granted
            }.map { it.permission }
        },
    ) { count, permissions ->
        HomeViewState(
            numOfLocations = count,
            notGrantedPermissions = permissions,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeViewState(),
        )
}
