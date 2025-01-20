package com.ramitsuri.locationtracking.data

import androidx.room.TypeConverter
import com.ramitsuri.locationtracking.model.BatteryStatus
import com.ramitsuri.locationtracking.model.MonitoringMode
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
}
