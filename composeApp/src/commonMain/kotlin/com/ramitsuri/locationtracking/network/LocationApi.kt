package com.ramitsuri.locationtracking.network

import com.ramitsuri.locationtracking.model.Location
import kotlinx.datetime.Instant

interface LocationApi {
    suspend fun postLocation(location: Location, deviceName: String, baseUrl: String): Result<Unit>

    suspend fun getLocations(
        deviceName: String,
        baseUrl: String,
        fromDate: Instant,
        toDate: Instant,
    ): Result<List<Location>>
}
