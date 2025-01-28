package com.ramitsuri.locationtracking.tracking.battery

import com.ramitsuri.locationtracking.model.BatteryStatus

interface BatteryInfoProvider {
    fun getChargingStatus(): BatteryStatus
    fun getLevel(): Int
}
