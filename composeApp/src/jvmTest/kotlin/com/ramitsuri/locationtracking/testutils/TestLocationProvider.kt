package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.location.Request
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class TestLocationProvider : LocationProvider {
    var locationsFlow: Flow<Location> = flow { }

    override fun requestUpdates(request: Request): Flow<Location> {
        return locationsFlow
    }

    override suspend fun requestSingle(): Location? {
        return locationsFlow.firstOrNull()
    }
}
