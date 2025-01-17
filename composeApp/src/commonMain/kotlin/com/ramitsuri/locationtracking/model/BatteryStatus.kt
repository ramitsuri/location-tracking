package com.ramitsuri.locationtracking.model

import com.ramitsuri.locationtracking.data.DbEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BatteryStatus(override val value: String) : DbEnum {
    @SerialName("0")
    UNKNOWN("unknown"),

    @SerialName("1")
    UNPLUGGED("unplugged"),

    @SerialName("2")
    CHARGING("charging"),

    @SerialName("3")
    FULL("full"),
}
