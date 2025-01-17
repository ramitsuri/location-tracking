package com.ramitsuri.locationtracking.tracking.wifi

import com.ramitsuri.locationtracking.model.WifiInfo
import kotlinx.coroutines.flow.StateFlow

interface WifiInfoProvider {
    val wifiInfo: StateFlow<WifiInfo>

    fun requestUpdates()

    fun unrequestUpdates()
}
