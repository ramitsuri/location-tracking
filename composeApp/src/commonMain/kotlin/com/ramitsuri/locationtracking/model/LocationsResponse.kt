package com.ramitsuri.locationtracking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationsResponse(
    @SerialName("count")
    val count: Int,

    @SerialName("data")
    val locations: List<Location>,
)
