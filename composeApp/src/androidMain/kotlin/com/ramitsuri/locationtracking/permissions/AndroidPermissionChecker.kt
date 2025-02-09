package com.ramitsuri.locationtracking.permissions

import android.content.Context
import android.content.pm.PackageManager
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

    override fun hasPermission(permission: Permission): Boolean {
        return permission
            .asAndroidPermission()
            ?.let {
                ContextCompat.checkSelfPermission(
                    context,
                    it,
                ) == PackageManager.PERMISSION_GRANTED
            } ?: true
    }
}
