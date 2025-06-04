package com.ramitsuri.locationtracking.ui.home

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LocationsViewMode
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.permissions.PermissionResult
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

data class HomeViewState(
    val numOfLocations: Int = 0,
    val permissionsResults: List<PermissionResult> = listOf(),
    val isUploadWorkerRunning: Boolean = false,
    val isLoading: Boolean = false,
    val selectedLocation: Location? = null,
    val viewMode: ViewMode = ViewMode.LastKnownLocation(null),
    val regions: List<Region> = emptyList(),
    val timeZone: TimeZone,
) {
    sealed interface ViewMode {
        data class LocationsForDate(
            val fromDate: LocalDate,
            val fromTime: LocalTime,
            val toDate: LocalDate,
            val toTime: LocalTime,
            val locations: List<Location>,
            val mode: LocationsViewMode,
            val timeline: List<Event> = emptyList(),
        ) : ViewMode {
            data class Event(
                val type: Type,
                val time: LocalDateTime,
                val wifiName: String,
            ) {
                enum class Type {
                    START, CONNECTED, DISCONNECTED
                }
            }
        }

        data class LastKnownLocation(
            val location: Location? = null,
        ) : ViewMode
    }

    val missingPermissions = permissionsResults.filter { !it.granted }.map { it.permission }
}
