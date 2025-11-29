package com.ramitsuri.locationtracking.tracking.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo as AndroidWifiInfo
import android.os.Build
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.model.WifiInfo
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AndroidWifiInfoProvider(
    context: Context,
    private val scope: CoroutineScope,
) : WifiInfoProvider {
    private val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _wifiInfo = MutableStateFlow(WifiInfo())
    override val wifiInfo = _wifiInfo.asStateFlow()

    private var updatesRequested = false

    private var updateWifiInfoJob: Job? = null

    override fun requestUpdates() {
        if (updatesRequested) {
            return
        }
        callback?.let {
            manager.registerDefaultNetworkCallback(it)
            updatesRequested = true
        }
    }

    override fun unrequestUpdates() {
        if (!updatesRequested) {
            return
        }
        updatesRequested = false
        _wifiInfo.update { WifiInfo() }
        callback?.let { manager.unregisterNetworkCallback(it) }
    }

    private val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                val info = networkCapabilities.transportInfo
                if (info is AndroidWifiInfo) {
                    val ssid = info.getUnquotedSSID()
                    logI(TAG) { "onCapabilitiesChanged: Wifi network: $ssid" }
                    updateWifiInfo(WifiInfo(ssid, info.bssid))
                } else if (networkCapabilities.isVpnAndWifi()) {
                    logI(TAG) {
                        "onCapabilitiesChanged: is VPN and Wifi, assuming staying on previous wifi"
                    }
                } else {
                    logI(TAG) {
                        "onCapabilitiesChanged: not VPN or not Wifi, reporting as wifi lost"
                    }
                    updateWifiInfo()
                }
                super.onCapabilitiesChanged(network, networkCapabilities)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                logI(TAG) { "onLost" }
                if (manager.getNetworkCapabilities(network)?.transportInfo is AndroidWifiInfo) {
                    updateWifiInfo()
                }
            }
        }
    } else {
        null
    }

    private fun updateWifiInfo(wifiInfo: WifiInfo = WifiInfo()) {
        updateWifiInfoJob?.cancel()
        updateWifiInfoJob = scope.launch {
            delay(1.seconds)
            _wifiInfo.update { wifiInfo }
        }
    }

    private fun AndroidWifiInfo.getUnquotedSSID(): String =
        this.ssid.replace(Regex("^\"(.*)\"$"), "$1")

    private fun NetworkCapabilities.isVpnAndWifi() =
        !hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN) &&
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

    companion object {
        private const val TAG = "AndroidWifiInfoProvider"
    }
}
