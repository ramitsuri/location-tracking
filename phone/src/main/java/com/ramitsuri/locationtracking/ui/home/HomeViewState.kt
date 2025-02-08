package com.ramitsuri.locationtracking.ui.home

import com.ramitsuri.locationtracking.model.Location
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

data class HomeViewState(
    val numOfLocations: Int = 0,
    val permissionsNotGranted: Boolean = false,
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
        ) : ViewMode

        data class LastKnownLocation(
            val location: Location? = null,
        ) : ViewMode
    }
}
