package com.ramitsuri.locationtracking.permissions

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidPermissionMonitor(
    private val permissionChecker: PermissionChecker,
) : PermissionMonitor, DefaultLifecycleObserver {

    private val _permissionState = MutableStateFlow<List<PermissionResult>>(listOf())
    override val permissionState = _permissionState.asStateFlow()

    override fun monitorPermissions(permissions: List<Permission>, lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
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
