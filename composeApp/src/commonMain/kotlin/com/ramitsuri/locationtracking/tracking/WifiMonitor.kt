package com.ramitsuri.locationtracking.tracking

import com.ramitsuri.locationtracking.data.dao.SeenWifiDao
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WifiMonitor(
    private val wifiInfoProvider: WifiInfoProvider,
    private val seenWifiDao: SeenWifiDao,
    private val scope: CoroutineScope,
    private val settings: Settings,
) {
    private var wifiMonitoringModeRuleJob: Job? = null
    private var connectedSsid: String? = null

    @Suppress("KotlinConstantConditions")
    fun startMonitoring() {
        logI(TAG) { "startMonitoring" }
        wifiInfoProvider.requestUpdates()
        scope.launch {
            wifiInfoProvider.wifiInfo.collect { wifiInfo ->
                val (connectedTo, disconnectedFrom) = when {
                    // Previously not connected to a wifi, still not connected to a wifi
                    connectedSsid == null && wifiInfo.ssid == null ||
                        (connectedSsid == wifiInfo.ssid) -> {
                        logI(TAG) {
                            "staying disconnected or " +
                                "connected to same wifi $connectedSsid -> ${wifiInfo.ssid}"
                        }
                        return@collect
                    }

                    // Previously not connected to a wifi, now connected to a wifi
                    connectedSsid == null && wifiInfo.ssid != null -> {
                        logI(TAG) { "connecting to wifi ${wifiInfo.ssid}" }
                        (wifiInfo.ssid to null)
                            .also {
                                connectedSsid = wifiInfo.ssid
                            }
                    }

                    // Previously connected to a wifi, now not connected to a wifi
                    connectedSsid != null && wifiInfo.ssid == null -> {
                        logI(TAG) { "disconnecting from wifi $connectedSsid" }
                        (null to connectedSsid)
                            .also {
                                connectedSsid = null
                            }
                    }

                    // Previously connected to a wifi, now connected to a different wifi
                    else -> {
                        logI(TAG) { "switching from wifi $connectedSsid to ${wifiInfo.ssid}" }
                        (wifiInfo.ssid to connectedSsid)
                            .also {
                                connectedSsid = wifiInfo.ssid
                            }
                    }
                }
                connectedTo?.let { seenWifiDao.upsert(it) }
                onWifiChanged(connectedTo, disconnectedFrom)
            }
        }
    }

    private suspend fun onWifiChanged(connectedTo: String?, disconnectedFrom: String?) =
        coroutineScope {
            wifiMonitoringModeRuleJob?.cancel()
            wifiMonitoringModeRuleJob = launch {
                delay(1.seconds)
                seenWifiDao
                    .getFavorites(listOfNotNull(connectedTo, disconnectedFrom))
                    .let { seenWifis ->
                        var modeToSet: MonitoringMode? = null
                        disconnectedFrom?.let { disconnectedSsid ->
                            if (seenWifis.any { it.ssid == disconnectedSsid }) {
                                modeToSet = MonitoringMode.Move
                            }
                        }
                        connectedTo?.let { connectedSsid ->
                            if (seenWifis.any { it.ssid == connectedSsid }) {
                                modeToSet = MonitoringMode.Rest
                            }
                        }
                        modeToSet?.let {
                            logI(TAG) { "Setting mode to $modeToSet" }
                            settings.setMonitoringMode(it)
                        }
                    }
            }
        }

    companion object {
        private const val TAG = "WifiMonitor"
    }
}
