package com.ramitsuri.locationtracking.network.impl

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LocationsResponse
import com.ramitsuri.locationtracking.network.LocationApi
import com.ramitsuri.locationtracking.network.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.datetime.Instant

internal class LocationApiImpl(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : LocationApi {
    override suspend fun postLocation(
        location: Location,
        deviceName: String,
        baseUrl: String,
    ): Result<Unit> {
        return apiRequest(ioDispatcher) {
            httpClient.post("$baseUrl/pub") {
                headers {
                    append("X-Limit-D", deviceName)
                }
                setBody(location)
            }
        }
    }

    override suspend fun getLocations(
        deviceName: String,
        baseUrl: String,
        fromDate: Instant,
        toDate: Instant,
    ): Result<List<Location>> {
        return apiRequest<LocationsResponse>(ioDispatcher) {
            httpClient.get("$baseUrl/api/0/locations") {
                url {
                    parameters.append("user", "owntracks")
                    parameters.append("device", deviceName)
                    parameters.append("from", fromDate.toString())
                    parameters.append("to", toDate.toString())
                }
            }
        }.map {
            it.locations
        }
    }
}
