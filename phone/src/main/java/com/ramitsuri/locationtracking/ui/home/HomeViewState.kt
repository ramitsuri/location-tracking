package com.ramitsuri.locationtracking.ui.home

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LocationsViewMode
import com.ramitsuri.locationtracking.permissions.PermissionResult
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

data class HomeViewState(
    val numOfLocations: Int = 0,
    val permissionsResults: List<PermissionResult> = listOf(),
    val isUploadWorkerRunning: Boolean = false,
    val isLoading: Boolean = false,
    val selectedLocation: Location? = null,
    val viewMode: ViewMode = ViewMode.LastKnownLocation(null),
    val timeZone: TimeZone,
) {
    sealed interface ViewMode {
        data class LocationsForDate(
            val date: LocalDate,
            val locations: List<Location>,
            val mode: LocationsViewMode,
        ) : ViewMode

        data class LastKnownLocation(
            val location: Location? = null,
        ) : ViewMode
    }

    val missingPermissions = permissionsResults.filter { !it.granted }.map { it.permission }
}
