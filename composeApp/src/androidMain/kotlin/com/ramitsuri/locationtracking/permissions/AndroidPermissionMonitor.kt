package com.ramitsuri.locationtracking.permissions

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidPermissionMonitor(
    private val permissionChecker: PermissionChecker,
    activity: ComponentActivity,
) : DefaultLifecycleObserver {
    init {
        activity.lifecycle.addObserver(this)
    }

    private val _permissionState = MutableStateFlow<List<PermissionResult>>(listOf())
    val permissionState = _permissionState.asStateFlow()

    fun monitorPermissions(permissions: List<Permission>) {
        _permissionState.update {
            permissionChecker.hasPermissions(permissions)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        _permissionState.update { permissionResults ->
            permissionChecker.hasPermissions(permissionResults.map { it.permission })
        }
    }
}
