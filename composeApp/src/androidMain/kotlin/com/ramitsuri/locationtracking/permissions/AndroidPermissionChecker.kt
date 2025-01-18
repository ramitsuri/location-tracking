package com.ramitsuri.locationtracking.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver

class AndroidPermissionChecker(
    context: Context,
) : PermissionChecker, DefaultLifecycleObserver {

    private val context = context.applicationContext

    override fun hasPermissions(permissions: List<Permission>): List<PermissionResult> {
        return permissions.map {
            PermissionResult(it, hasPermission(it))
        }
    }

    private fun hasPermission(permission: Permission): Boolean {
        return permission
            .asAndroidPermission()
            ?.let {
                ContextCompat.checkSelfPermission(
                    context,
                    it,
                ) == PackageManager.PERMISSION_GRANTED
            } ?: true
    }

    private fun Permission.asAndroidPermission(): String? {
        return when (this) {
            Permission.FINE_LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
            Permission.COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
            Permission.NOTIFICATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else {
                null
            }
        }
    }
}
