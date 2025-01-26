package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ramitsuri.locationtracking.data.DbEnum
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "wifi_monitoring_mode_rules",
)
data class WifiMonitoringModeRule
@OptIn(ExperimentalUuidApi::class)
constructor(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "ssid")
    val ssid: String,

    @ColumnInfo(name = "status")
    val status: Status,

    @ColumnInfo(name = "mode")
    val mode: MonitoringMode,
) {
    enum class Status(override val value: String) : DbEnum {
        UNKNOWN("unknown"),
        CONNECTED("connected"),
        DISCONNECTED("disconnected"),
    }
}
