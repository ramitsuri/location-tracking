package com.ramitsuri.locationtracking.ui.home

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.permissions.Permission

data class HomeViewState(
    val numOfLocations: Int = 0,
    val notGrantedPermissions: List<Permission> = emptyList(),
    val isUploadWorkerRunning: Boolean = false,
    val lastKnownLocation: Location? = null,
)
