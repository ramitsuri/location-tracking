package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ramitsuri.locationtracking.network.InstantSerializer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
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

    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    @ColumnInfo(name = "createdAt")
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
    @Serializable(with = InstantSerializer::class)
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
) {
    @SerialName("_http")
    @Required
    @Ignore
    val http: Boolean = true

    @SerialName("_type")
    @Required
    @Ignore
    val type: String = "location"

    override fun toString(): String {
        return "lat: $latitude, lng: $longitude, acc: $accuracy, wifi: $ssid"
    }
}
