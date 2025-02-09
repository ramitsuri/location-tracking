package com.ramitsuri.locationtracking.permissions

import android.Manifest
import android.os.Build

fun Permission.asAndroidPermission(): String? {
    return when (this) {
        Permission.FINE_LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        Permission.COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
        Permission.NOTIFICATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }

        Permission.ACCESS_BACKGROUND_LOCATION -> Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }
}
