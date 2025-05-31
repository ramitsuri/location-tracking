package com.ramitsuri.locationtracking.network.impl

import com.ramitsuri.locationtracking.model.GithubLatestRelease
import com.ramitsuri.locationtracking.network.GithubApi
import com.ramitsuri.locationtracking.network.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher

internal class GithubApiImpl(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : GithubApi {
    override suspend fun getLatestRelease(): Result<GithubLatestRelease> {
        return apiRequest(ioDispatcher) {
            httpClient.get(URL)
        }
    }

    override suspend fun downloadAndSave(url: String, toFile: File) {
        httpClient.prepareGet(url).execute { resp ->
            resp.bodyAsChannel().copyAndClose(toFile.writeChannel())
        }
    }

    companion object {
        private const val URL =
            "https://api.github.com/repos/ramitsuri/location-tracking/releases/latest"
    }
}
