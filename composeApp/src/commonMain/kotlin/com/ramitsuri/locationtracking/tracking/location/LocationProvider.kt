package com.ramitsuri.locationtracking.tracking.location

import com.ramitsuri.locationtracking.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    fun requestUpdates(request: Request): Flow<Location>

    suspend fun requestSingle(): Location?
}
