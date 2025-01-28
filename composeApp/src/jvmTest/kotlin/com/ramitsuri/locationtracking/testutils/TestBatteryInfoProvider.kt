package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.model.BatteryStatus
import com.ramitsuri.locationtracking.tracking.battery.BatteryInfoProvider

class TestBatteryInfoProvider : BatteryInfoProvider {
    override fun getChargingStatus(): BatteryStatus {
        return BatteryStatus.CHARGING
    }

    override fun getLevel(): Int {
        return 100
    }
}
