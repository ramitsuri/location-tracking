package com.ramitsuri.locationtracking.permissions

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.StateFlow

interface PermissionMonitor {
    val permissionState: StateFlow<List<PermissionResult>>

    fun monitorPermissions(permissions: List<Permission>, lifecycle: Lifecycle)
}
