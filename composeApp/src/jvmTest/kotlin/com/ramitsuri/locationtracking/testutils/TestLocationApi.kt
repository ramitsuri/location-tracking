package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.network.LocationApi
import kotlinx.datetime.Instant

class TestLocationApi : LocationApi {
    override suspend fun postLocation(
        location: Location,
        deviceName: String,
        baseUrl: String,
    ): Result<Unit> {
        println("Posted")
        return Result.success(Unit)
    }

    override suspend fun getLocations(
        deviceName: String,
        baseUrl: String,
        fromDate: Instant,
        toDate: Instant,
    ): Result<List<Location>> {
        return Result.success(emptyList())
    }
}
