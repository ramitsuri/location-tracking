package com.ramitsuri.locationtracking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BatteryStatus {
    @SerialName("0")
    UNKNOWN,

    @SerialName("1")
    UNPLUGGED,

    @SerialName("2")
    CHARGING,

    @SerialName("3")
    FULL,
}
