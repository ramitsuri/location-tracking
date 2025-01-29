package com.ramitsuri.locationtracking

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.notification.NotificationManager
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.permissions.PermissionMonitor
import com.ramitsuri.locationtracking.services.BackgroundService
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
                onServiceStop = ::stopService,
            )
        }
    }

    private fun navToSystemSettings() {
        startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            },
        )
    }

    private fun startService(action: String? = null) {
        logD(TAG) { "requesting service start" }
        if (
            (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    !permissionChecker.hasPermission(Permission.ACCESS_BACKGROUND_LOCATION)
                ) ||
            !permissionChecker.hasPermissions(
                listOf(
                    Permission.FINE_LOCATION,
                    Permission.COARSE_LOCATION,
                ),
            ).any { it.granted }
        ) {
            notificationManager.notifyBackgroundLocationRestriction(
                title = getString(R.string.fg_service_restriction_title),
                text = getString(R.string.fg_service_restriction_text),
            )
            logW(TAG) { "can't start location fg service without bg location permission" }
            return
        }
        ContextCompat.startForegroundService(
            this,
            Intent()
                .setClass(this, BackgroundService::class.java)
                .apply {
                    action?.also { this.action = it }
                },
        )
    }

    private fun stopService() {
        logD(TAG) { "requesting service stop" }
        stopService(Intent(this, BackgroundService::class.java))
    }

    private fun killApp() {
        stopService(Intent(this, BackgroundService::class.java))
        finishAffinity()
        Process.killProcess(Process.myPid())
    }

    companion object {
        private const val TAG = "MainActivity"
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
