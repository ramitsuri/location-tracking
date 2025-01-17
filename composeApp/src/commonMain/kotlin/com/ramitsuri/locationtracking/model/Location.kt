package com.ramitsuri.locationtracking.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Location(
    @SerialName("_id")
    val messageId: String = Uuid.random().toString(),

    @SerialName("created_at")
    val createdAt: Instant = Clock.System.now(),

    @SerialName("lat")
    val latitude: Double,

    @SerialName("lon")
    val longitude: Double,

    @SerialName("alt")
    val altitude: Int,

    @SerialName("acc")
    val accuracy: Int,

    @SerialName("vac")
    val verticalAccuracy: Int,

    @SerialName("cog")
    val bearing: Int,

    @SerialName("tst")
    val locationTimestamp: Instant,

    @SerialName("vel")
    val velocity: Int,

    @SerialName("t")
    var trigger: String = "",

    @SerialName("batt")
    val battery: Int? = null,

    @SerialName("bs")
    val batteryStatus: BatteryStatus? = null,

    @SerialName("m")
    val monitoringMode: MonitoringMode? = null,

    @SerialName("inregions")
    val inRegions: List<String>? = null,

    @SerialName("BSSID")
    val bssid: String? = null,

    @SerialName("SSID")
    val ssid: String? = null,

    @SerialName("tid")
    val trackerId: String? = null,
)
