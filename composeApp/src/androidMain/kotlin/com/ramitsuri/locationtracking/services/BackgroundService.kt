package com.ramitsuri.locationtracking.services

import android.app.ActivityManager
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Process
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.notification.NotificationConstants
import com.ramitsuri.locationtracking.notification.NotificationManager
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.Tracker
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class BackgroundService : LifecycleService(), KoinComponent {
    private val permissionChecker: PermissionChecker by inject()
    private val settings: Settings by inject()
    private val tracker by lazy {
        Tracker(
            permissionChecker = permissionChecker,
            locationProvider = get<LocationProvider>(),
            wifiInfoProvider = get<WifiInfoProvider>(),
            locationRepository = get<LocationRepository>(),
            settings = settings,
            scope = lifecycleScope,
        )
    }
    private val notificationManager by lazy {
        NotificationManager(context = applicationContext, permissionChecker = permissionChecker)
    }
    private val activityManager by lazy {
        getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    private var hasBeenStartedExplicitly = false

    override fun onCreate() {
        logD(TAG) { "onCreate" }
        super.onCreate()
    }

    override fun onDestroy() {
        logD(TAG) { "onDestroy" }
        stopForeground(STOP_FOREGROUND_REMOVE)
        tracker.stopTracking()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logD(TAG) { "onStartCommand intent=$intent" }
        super.onStartCommand(intent, flags, startId)
        handleIntent(intent)
        return START_STICKY
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != null) {
            logI(TAG) { "intent received with action:${intent.action}" }
            when (intent.action) {
                INTENT_ACTION_SEND_LOCATION_USER -> {
                    tracker.trackSingle()
                }

                INTENT_ACTION_CHANGE_MONITORING -> {
                    changeMonitoringMode()
                }

                INTENT_ACTION_BOOT_COMPLETED,
                INTENT_ACTION_PACKAGE_REPLACED,
                -> {
                    onDeviceRestartOrAppUpdated()
                }

                INTENT_ACTION_EXIT -> {
                    exit()
                }

                else -> {}
            }
        } else {
            logI(TAG) { "no intent or action provided, setting up location request" }
            hasBeenStartedExplicitly = true
            setupAndStartService()
        }
    }

    private fun changeMonitoringMode() {
        logD(TAG) { "changeMonitoringMode" }
        lifecycleScope.launch {
            settings.setNextMonitoringMode()
            hasBeenStartedExplicitly = true
            notificationManager.cancelBackgroundRestrictionNotification()
        }
    }

    private fun onDeviceRestartOrAppUpdated() {
        logI(TAG) { "onDeviceRestartOrAppUpdated" }
        if (!permissionChecker.hasPermission(Permission.ACCESS_BACKGROUND_LOCATION) &&
            !hasBeenStartedExplicitly
        ) {
            notifyBackgroundLocationRestriction()
        }
        setupAndStartService()
    }

    private fun startForegroundService() {
        logD(TAG) { "startForegroundService" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            lifecycleScope.launch {
                try {
                    val mode = settings.getMonitoringMode().first()
                    startForeground(
                        NotificationConstants.NOTIFICATION_ONGOING_ID,
                        getOngoingNotification(mode),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
                    )
                } catch (e: ForegroundServiceStartNotAllowedException) {
                    logE(TAG, e) {
                        "Foreground service start not allowed. " +
                            "backgroundRestricted=${activityManager.isBackgroundRestricted}"
                    }
                    return@launch
                }
            }
        } else {
            lifecycleScope.launch {
                val mode = settings.getMonitoringMode().first()
                startForeground(
                    NotificationConstants.NOTIFICATION_ONGOING_ID,
                    getOngoingNotification(mode),
                )
            }
        }
    }

    private fun setupAndStartService() {
        logD(TAG) { "setupAndStartService" }
        startForegroundService()
        tracker.startTracking()
        lifecycleScope.launch {
            settings.getMonitoringMode().collect { mode ->
                notifyOngoing(mode)
            }
        }
    }

    private fun exit() {
        logI(TAG) { "exit() called. Stopping service and process." }
        stopSelf()
        Process.killProcess(Process.myPid())
    }

    private fun notifyBackgroundLocationRestriction() {
        notificationManager.notifyBackgroundLocationRestriction(
            title = getString(R.string.background_location_restriction_title),
            text = getString(R.string.background_location_restriction_text),
        )
    }

    private fun notifyOngoing(mode: MonitoringMode) {
        notificationManager.notify(getOngoingNotification(mode))
    }

    private fun getOngoingNotification(mode: MonitoringMode): Notification {
        return notificationManager.getOngoingNotification(
            title = getString(R.string.app_name),
            publishActionLabel = getString(R.string.notification_publish),
            changeMonitoringActionLabel = getString(R.string.notification_change_monitoring),
            modeLabel = mode.label(),
        )
    }

    private fun MonitoringMode.label() = when (this) {
        MonitoringMode.Quiet -> R.string.monitoring_mode_quiet
        MonitoringMode.Slow -> R.string.monitoring_mode_manual
        MonitoringMode.Significant -> R.string.monitoring_mode_significant_changes
        MonitoringMode.Move -> R.string.monitoring_mode_move
    }.let {
        getString(it)
    }

    companion object {
        private const val TAG = "BackgroundService"

        // NEW ACTIONS ALSO HAVE TO BE ADDED TO THE SERVICE INTENT FILTER
        const val INTENT_ACTION_SEND_LOCATION_USER =
            "com.ramitsuri.locationtracking.SEND_LOCATION_USER"
        const val INTENT_ACTION_CHANGE_MONITORING =
            "com.ramitsuri.locationtracking.CHANGE_MONITORING"
        private const val INTENT_ACTION_EXIT = "com.ramitsuri.locationtracking.EXIT"
        private const val INTENT_ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
        private const val INTENT_ACTION_PACKAGE_REPLACED =
            "android.intent.action.MY_PACKAGE_REPLACED"
    }
}
