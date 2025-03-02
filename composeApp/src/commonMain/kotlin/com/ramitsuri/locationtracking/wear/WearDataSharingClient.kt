package com.ramitsuri.locationtracking.wear

import com.ramitsuri.locationtracking.model.MonitoringMode

interface WearDataSharingClient {
    suspend fun postMonitoringMode(mode: MonitoringMode, to: To): Boolean

    suspend fun postSingleLocation(to: To): Boolean

    enum class To {
        Phone,
        Wear,
    }
}
