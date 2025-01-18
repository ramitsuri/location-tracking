package com.ramitsuri.locationtracking.network.impl

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.network.LocationApi
import com.ramitsuri.locationtracking.network.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineDispatcher

internal class LocationApiImpl(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : LocationApi {
    override suspend fun postLocation(location: Location): Result<Unit> {
        return apiRequest(ioDispatcher) {
            httpClient.post("$baseUrl/pub") {
                setBody(location)
            }
        }
    }
}
