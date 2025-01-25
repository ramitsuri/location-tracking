package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.network.LocationApi

class TestLocationApi : LocationApi {
    override suspend fun postLocation(
        location: Location,
        deviceName: String,
        baseUrl: String,
    ): Result<Unit> {
        println("Posted")
        return Result.success(Unit)
    }
}
