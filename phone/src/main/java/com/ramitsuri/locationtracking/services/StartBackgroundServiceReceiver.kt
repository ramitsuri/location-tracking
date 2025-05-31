package com.ramitsuri.locationtracking.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.notification.NotificationManager
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.services.BackgroundService.Companion.startBackgroundService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StartBackgroundServiceReceiver : BroadcastReceiver(), KoinComponent {
    private val permissionChecker by inject<PermissionChecker>()
    private val notificationManager by inject<NotificationManager>()

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED == intent.action ||
            Intent.ACTION_BOOT_COMPLETED == intent.action
        ) {
            logI(TAG) { "${intent.action} received" }
            context.startBackgroundService(
                action = intent.action,
                permissionChecker = permissionChecker,
                notificationManager = notificationManager,
            )
        }
    }

    companion object {
        private const val TAG = "StartBackgroundServiceReceiver"
    }
}
