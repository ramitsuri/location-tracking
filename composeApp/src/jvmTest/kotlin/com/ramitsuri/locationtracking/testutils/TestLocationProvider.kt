package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.location.Request
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull

class TestLocationProvider : LocationProvider {
    val locationsFlow = MutableStateFlow<Location?>(null)

    override fun requestUpdates(request: Request): Flow<Location> {
        return locationsFlow.filterNotNull()
    }

    override suspend fun requestSingle(): Location? {
        return locationsFlow.firstOrNull()
    }
}
