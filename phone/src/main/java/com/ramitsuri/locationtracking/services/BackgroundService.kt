package com.ramitsuri.locationtracking.services

import android.app.ActivityManager
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Process
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.data.dao.SeenWifiDao
import com.ramitsuri.locationtracking.data.dao.WifiMonitoringModeRuleDao
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.notification.NotificationConstants
import com.ramitsuri.locationtracking.notification.NotificationManager
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.Tracker
import com.ramitsuri.locationtracking.tracking.WifiMonitor
import com.ramitsuri.locationtracking.tracking.battery.BatteryInfoProvider
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import com.ramitsuri.locationtracking.ui.label
import com.ramitsuri.locationtracking.util.getIcon
import com.ramitsuri.locationtracking.wear.WearDataSharingClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Duration.Companion.seconds

class BackgroundService : LifecycleService(), KoinComponent {
    private val permissionChecker: PermissionChecker by inject()
    private val settings: Settings by inject()
    private val notificationManager: NotificationManager by inject()
    private val wearDataSharingClient: WearDataSharingClient by inject()
    private val tracker by lazy {
        Tracker(
            permissionChecker = permissionChecker,
            locationProvider = get<LocationProvider>(),
            wifiInfoProvider = get<WifiInfoProvider>(),
            batteryInfoProvider = get<BatteryInfoProvider>(),
            locationRepository = get<LocationRepository>(),
            geocoderRepository = get<GeocoderRepository>(),
            settings = settings,
            scope = lifecycleScope,
        )
    }
    private val wifiMonitor by lazy {
        WifiMonitor(
            wifiInfoProvider = get<WifiInfoProvider>(),
            settings = settings,
            scope = lifecycleScope,
            seenWifiDao = get<SeenWifiDao>(),
            wifiMonitoringModeRuleDao = get<WifiMonitoringModeRuleDao>(),
        )
    }
    private val activityManager by lazy {
        getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    private var hasBeenStartedExplicitly = false

    private var postToWearOsJob: Job? = null

    override fun onCreate() {
        logD(TAG) { "onCreate" }
        super.onCreate()
    }

    override fun onDestroy() {
        logD(TAG) { "onDestroy" }
        stopForeground(STOP_FOREGROUND_REMOVE)
        tracker.stopTracking()
        _isRunning.update { false }
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

                INTENT_ACTION_CHANGE_MONITORING_NEXT -> {
                    changeMonitoringMode(isNextNext = false)
                }

                INTENT_ACTION_CHANGE_MONITORING_NEXT_NEXT -> {
                    changeMonitoringMode(isNextNext = true)
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

    private fun changeMonitoringMode(isNextNext: Boolean) {
        logD(TAG) { "changeMonitoringMode" }
        lifecycleScope.launch {
            settings.getMonitoringMode().first()
                .let {
                    val mode = if (isNextNext) {
                        it.getNextMode().getNextMode()
                    } else {
                        it.getNextMode()
                    }
                    settings.setMonitoringMode(mode)
                }
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

    private suspend fun startForegroundService(): Boolean {
        logD(TAG) { "startForegroundService" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val mode = settings.getMonitoringMode().first()
                startForeground(
                    NotificationConstants.NOTIFICATION_ONGOING_ID,
                    getOngoingNotification(mode),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
                )
                _isRunning.update { true }
            } catch (e: ForegroundServiceStartNotAllowedException) {
                logE(TAG, e) {
                    "Foreground service start not allowed. " +
                            "backgroundRestricted=${activityManager.isBackgroundRestricted}"
                }
                notifyBackgroundLocationRestriction()
                return false
            }
        } else {
            val mode = settings.getMonitoringMode().first()
            startForeground(
                NotificationConstants.NOTIFICATION_ONGOING_ID,
                getOngoingNotification(mode),
            )
            _isRunning.update { true }
        }
        return true
    }

    private fun setupAndStartService() {
        logD(TAG) { "setupAndStartService" }
        lifecycleScope.launch {
            if (isRunning.value) {
                logD(TAG) { "already running" }
                return@launch
            }
            val started = startForegroundService()
            if (!started) {
                return@launch
            }
            tracker.startTracking()
            wifiMonitor.startMonitoring()
            launch {
                combine(
                    settings.getMonitoringMode(),
                    tracker.lastKnownAddressOrLocation,
                ) { mode, addressOrLocation ->
                    mode to addressOrLocation
                }.collect { (mode, addressOrLocation) ->
                    notifyOngoing(mode, addressOrLocation?.string())
                }
            }
            launch {
                settings.getMonitoringMode().collect {
                    postToWearOsJob?.cancel()
                    postToWearOsJob = launch {
                        delay(3.seconds)
                        wearDataSharingClient.postMonitoringMode(
                            mode = it,
                            to = WearDataSharingClient.To.Wear,
                        )
                    }
                }
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

    private fun notifyOngoing(mode: MonitoringMode, title: String? = null) {
        notificationManager.notify(getOngoingNotification(mode, title))
    }

    private fun getOngoingNotification(
        mode: MonitoringMode,
        title: String? = null,
    ): Notification {
        val nextMode = mode.getNextMode()
        val nextNextMode = nextMode.getNextMode()
        return notificationManager.getOngoingNotification(
            title = title ?: getString(R.string.app_name),
            publishActionLabel = getString(R.string.notification_publish),
            modeLabel = mode.label(context = this),
            nextModeLabel = nextMode.label(this),
            nextNextModeLabel = nextNextMode.label(this),
            icon = mode.getIcon(),
        )
    }

    companion object {
        private const val TAG = "BackgroundService"

        private val _isRunning = MutableStateFlow(false)
        val isRunning = _isRunning.asStateFlow()

        // NEW ACTIONS ALSO HAVE TO BE ADDED TO THE SERVICE INTENT FILTER
        const val INTENT_ACTION_SEND_LOCATION_USER =
            "com.ramitsuri.locationtracking.SEND_LOCATION_USER"
        const val INTENT_ACTION_CHANGE_MONITORING_NEXT =
            "com.ramitsuri.locationtracking.CHANGE_MONITORING_NEXT"
        const val INTENT_ACTION_CHANGE_MONITORING_NEXT_NEXT =
            "com.ramitsuri.locationtracking.CHANGE_MONITORING_NEXT_NEXT"
        private const val INTENT_ACTION_EXIT = "com.ramitsuri.locationtracking.EXIT"
        private const val INTENT_ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
        private const val INTENT_ACTION_PACKAGE_REPLACED =
            "android.intent.action.MY_PACKAGE_REPLACED"

        fun Context.startBackgroundService(
            action: String? = null,
            permissionChecker: PermissionChecker,
            notificationManager: NotificationManager
        ) {
            logD(TAG) { "requesting service start with action=$action" }
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                        !permissionChecker.hasPermission(Permission.ACCESS_BACKGROUND_LOCATION)) ||
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

        fun Context.stopBackgroundService() {
            logD(TAG) { "requesting service stop" }
            val intent = Intent(this, BackgroundService::class.java)
            stopService(intent)
        }
    }
}
