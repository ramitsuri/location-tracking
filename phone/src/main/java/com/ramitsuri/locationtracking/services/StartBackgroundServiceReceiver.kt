package com.ramitsuri.locationtracking.services

import android.app.ForegroundServiceStartNotAllowedException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logI

class StartBackgroundServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED == intent.action ||
            Intent.ACTION_BOOT_COMPLETED == intent.action
        ) {
            logI(TAG) { "android.intent.action.BOOT_COMPLETED received" }
            val startIntent = Intent(context, BackgroundService::class.java)
            startIntent.action = intent.action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    context.startForegroundService(startIntent)
                } catch (e: ForegroundServiceStartNotAllowedException) {
                    logE(TAG) {
                        "Unable to start foreground service, because Android has prevented it. " +
                            "This should not happen if intent action is " +
                            "${Intent.ACTION_MY_PACKAGE_REPLACED} or " +
                            "${Intent.ACTION_BOOT_COMPLETED}. intent action was ${intent.action}"
                    }
                }
            } else {
                context.startForegroundService(startIntent)
            }
        }
    }

    companion object {
        private const val TAG = "StartBackgroundServiceReceiver"
    }
}
