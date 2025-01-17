package com.ramitsuri.locationtracking.network

import com.ramitsuri.locationtracking.model.WifiInfo
import kotlinx.coroutines.flow.StateFlow

interface WifiInfoProvider {
    val wifiInfo: StateFlow<WifiInfo>

    fun requestUpdates()

    fun unrequestUpdates()
}
