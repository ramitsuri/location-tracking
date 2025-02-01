package com.ramitsuri.locationtracking.tracking.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo as AndroidWifiInfo
import android.os.Build
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.model.WifiInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidWifiInfoProvider(context: Context) : WifiInfoProvider {
    private val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _wifiInfo = MutableStateFlow(WifiInfo())
    override val wifiInfo = _wifiInfo.asStateFlow()

    private var updatesRequested = false

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
                logI(TAG) {
                    "onCapabilitiesChanged: $networkCapabilities"
                }
                (networkCapabilities.transportInfo as? AndroidWifiInfo)
                    ?.let { info ->
                        _wifiInfo.update {
                            WifiInfo(info.getUnquotedSSID(), info.bssid)
                        }
                    }
                super.onCapabilitiesChanged(network, networkCapabilities)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                logI(TAG) {
                    "onLost network - $network, capabilities - ${manager.getNetworkCapabilities(
                        network,
                    )}"
                }
                if (manager.getNetworkCapabilities(network)?.transportInfo is AndroidWifiInfo) {
                    _wifiInfo.update { WifiInfo() }
                }
            }
        }
    } else {
        null
    }

    private fun AndroidWifiInfo.getUnquotedSSID(): String =
        this.ssid.replace(Regex("^\"(.*)\"$"), "$1")

    companion object {
        private const val TAG = "AndroidWifiInfoProvider"
    }
}
