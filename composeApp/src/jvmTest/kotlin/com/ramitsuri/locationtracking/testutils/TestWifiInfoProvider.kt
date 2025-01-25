package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.model.WifiInfo
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import kotlinx.coroutines.flow.MutableStateFlow

class TestWifiInfoProvider : WifiInfoProvider {
    override val wifiInfo: MutableStateFlow<WifiInfo> = MutableStateFlow(WifiInfo())

    override fun requestUpdates() {
        println("Requesting updates")
    }

    override fun unrequestUpdates() {
        println("Unrequesting updates")
    }
}
