package com.ramitsuri.locationtracking.network

import com.ramitsuri.locationtracking.model.GithubLatestRelease
import java.io.File

interface GithubApi {
    suspend fun getLatestRelease(): Result<GithubLatestRelease>

    suspend fun downloadAndSave(url: String, toFile: File)
}
