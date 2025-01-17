package com.ramitsuri.locationtracking.model

import com.ramitsuri.locationtracking.data.DbEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MonitoringMode(override val value: String):DbEnum {
    @SerialName("0")
    Quiet("quiet"),

    @SerialName("1")
    Manual("manual"),

    @SerialName("2")
    Significant("significant"),

    @SerialName("3")
    Move("move"),
}
