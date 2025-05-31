package com.ramitsuri.locationtracking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ramitsuri.locationtracking.notification.NotificationManager
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.permissions.PermissionMonitor
import com.ramitsuri.locationtracking.services.BackgroundService
import com.ramitsuri.locationtracking.services.BackgroundService.Companion.startBackgroundService
import com.ramitsuri.locationtracking.services.BackgroundService.Companion.stopBackgroundService
import com.ramitsuri.locationtracking.ui.navigation.NavGraph
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val permissionChecker by inject<PermissionChecker>()
    private val notificationManager by inject<NotificationManager>()

    private val permissionMonitor by inject<PermissionMonitor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        startService()
        permissionMonitor.monitorPermissions(Permission.entries, this.lifecycle)
        enableEdgeToEdge()
        setContent {
            val darkTheme = isSystemInDarkTheme()
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle =
                        SystemBarStyle.auto(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                        ) { darkTheme },
                    navigationBarStyle =
                        SystemBarStyle.auto(
                            lightScrim,
                            darkScrim,
                        ) { darkTheme },
                )
                onDispose {}
            }
            NavGraph(
                onKillApp = ::killApp,
                onSingleLocation = {
                    startService(
                        action = BackgroundService.INTENT_ACTION_SEND_LOCATION_USER,
                    )
                },
                onNavToSystemSettings = ::navToSystemSettings,
                onServiceStart = ::startService,
                onServiceStop = { stopBackgroundService() },
            )
        }
    }

    private fun navToSystemSettings() {
        startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:$packageName".toUri()
            },
        )
    }

    private fun startService(action: String? = null) {
        startBackgroundService(
            action = action,
            permissionChecker = permissionChecker,
            notificationManager = notificationManager,
        )
    }

    private fun killApp() {
        stopBackgroundService()
        finishAffinity()
        Process.killProcess(Process.myPid())
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:
 * activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;
 * drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:
 * activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;
 * drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
