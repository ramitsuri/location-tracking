package com.ramitsuri.locationtracking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MonitoringMode {
    @SerialName("0")
    Quiet,

    @SerialName("1")
    Manual,

    @SerialName("2")
    Significant,

    @SerialName("3")
    Move,
}
