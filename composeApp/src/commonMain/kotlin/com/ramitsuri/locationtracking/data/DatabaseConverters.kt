package com.ramitsuri.locationtracking.data

import androidx.room.TypeConverter
import com.ramitsuri.locationtracking.model.BatteryStatus
import com.ramitsuri.locationtracking.model.LogLevel
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.model.WifiMonitoringModeRule
import java.math.BigDecimal
import kotlinx.datetime.Instant

class DatabaseConverters {
    @TypeConverter
    fun toInstant(string: String): Instant {
        return Instant.parse(string)
    }

    @TypeConverter
    fun fromInstant(instant: Instant): String {
        return instant.toString()
    }

    @TypeConverter
    fun toBatteryStatus(string: String): BatteryStatus {
        return toEnum(string, BatteryStatus.UNKNOWN)
    }

    @TypeConverter
    fun fromBatteryStatus(batteryStatus: BatteryStatus): String {
        return batteryStatus.value
    }

    @TypeConverter
    fun toMonitoringMode(string: String): MonitoringMode {
        return toEnum(string, MonitoringMode.default())
    }

    @TypeConverter
    fun fromMonitoringMode(monitoringMode: MonitoringMode): String {
        return monitoringMode.value
    }

    @TypeConverter
    fun toWifiMonitoringModeRuleStatus(string: String): WifiMonitoringModeRule.Status {
        return toEnum(string, WifiMonitoringModeRule.Status.UNKNOWN)
    }

    @TypeConverter
    fun fromWifiMonitoringModeRuleStatus(status: WifiMonitoringModeRule.Status): String {
        return status.value
    }

    @TypeConverter
    fun toListOfString(string: String): List<String> {
        return string.split(";;;")
    }

    @TypeConverter
    fun fromListOfString(list: List<String>): String {
        return list.joinToString(";;;")
    }

    @TypeConverter
    fun toBigDecimal(string: String): BigDecimal {
        return BigDecimal(string)
    }

    @TypeConverter
    fun fromBigDecimal(bigDecimal: BigDecimal): String {
        return bigDecimal.toString()
    }

    @TypeConverter
    fun toLogLevel(string: String): LogLevel {
        return toEnum(string, LogLevel.DEBUG)
    }

    @TypeConverter
    fun fromLogLevel(logLevel: LogLevel): String {
        return logLevel.value
    }
}
