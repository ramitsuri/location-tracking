package com.ramitsuri.locationtracking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LocationsViewMode {

    @SerialName("Points")
    Points,

    @SerialName("Lines")
    Lines,

    @SerialName("Motion")
    Motion,
}
