package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Serializable
@Entity(tableName = "location")
data class Location(
    @SerialName("_id")
    @PrimaryKey
    @ColumnInfo(name = "id")
    val messageId: String = Uuid.random().toString(),

    @SerialName("createdAt")
    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now(),

    @SerialName("lat")
    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @SerialName("lon")
    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @SerialName("alt")
    @ColumnInfo(name = "altitude")
    val altitude: Int,

    @SerialName("acc")
    @ColumnInfo(name = "accuracy")
    val accuracy: Int,

    @SerialName("vac")
    @ColumnInfo(name = "verticalAccuracy")
    val verticalAccuracy: Int,

    @SerialName("cog")
    @ColumnInfo(name = "bearing")
    val bearing: Int,

    @SerialName("tst")
    @ColumnInfo(name = "locationTimestamp")
    val locationTimestamp: Instant,

    @SerialName("vel")
    @ColumnInfo(name = "velocity")
    val velocity: Int,

    @SerialName("t")
    @ColumnInfo(name = "trigger")
    var trigger: String = "",

    @SerialName("batt")
    @ColumnInfo(name = "battery")
    val battery: Int? = null,

    @SerialName("bs")
    @ColumnInfo(name = "batteryStatus")
    val batteryStatus: BatteryStatus? = null,

    @SerialName("m")
    @ColumnInfo(name = "monitoringMode")
    val monitoringMode: MonitoringMode? = null,

    @SerialName("inregions")
    @ColumnInfo(name = "inRegions")
    val inRegions: List<String>? = null,

    @SerialName("BSSID")
    @ColumnInfo(name = "bssid")
    val bssid: String? = null,

    @SerialName("SSID")
    @ColumnInfo(name = "ssid")
    val ssid: String? = null,

    @SerialName("tid")
    @ColumnInfo(name = "trackerId")
    val trackerId: String? = null,
)
