package com.ramitsuri.locationtracking.network

import com.ramitsuri.locationtracking.model.Location

interface LocationApi {
    suspend fun postLocation(location: Location, deviceName: String, baseUrl: String): Result<Unit>
}
