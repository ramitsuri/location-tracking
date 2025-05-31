package com.ramitsuri.locationtracking.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubLatestRelease(
    @SerialName("name")
    val name: String,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("assets")
    val assets: List<Asset>,
) {
    @Serializable
    data class Asset(
        @SerialName("name")
        val name: String,

        @SerialName("browser_download_url")
        val downloadUrl: String,
    )
}
