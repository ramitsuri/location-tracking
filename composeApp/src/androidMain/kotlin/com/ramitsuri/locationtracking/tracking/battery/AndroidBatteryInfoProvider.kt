package com.ramitsuri.locationtracking.tracking.battery

import android.content.Context
import android.os.BatteryManager
import com.ramitsuri.locationtracking.model.BatteryStatus

class AndroidBatteryInfoProvider(context: Context) : BatteryInfoProvider {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    override fun getChargingStatus(): BatteryStatus {
        return when (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)) {
            BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.UNPLUGGED
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.UNPLUGGED
            else -> BatteryStatus.UNKNOWN
        }
    }

    override fun getLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
