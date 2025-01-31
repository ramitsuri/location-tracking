package com.ramitsuri.locationtracking.repository

import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.network.LocationApi
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class LocationRepository(
    private val locationDao: LocationDao,
    private val locationApi: LocationApi,
    private val settings: Settings,
) {
    fun getLastKnownLocation(): Flow<Location?> {
        return settings.getLastKnownLocationFlow()
    }

    suspend fun insert(location: Location) {
        locationDao.insert(location)
    }

    suspend fun upload(): Result<Unit> {
        val baseUrl = settings.getBaseUrl()
        val deviceName = settings.getDeviceName()
        if (baseUrl.isEmpty() || deviceName.isEmpty()) {
            logE(TAG) { "Base URL: $baseUrl or device name: $deviceName is not set" }
            return Result.failure(Exception("Base URL or device name is not set"))
        }
        while (true) {
            val uploaded = mutableListOf<Location>()
            val locations = locationDao.get(limit = 10)
            if (locations.isEmpty()) {
                return Result.success(Unit)
            }
            locations
                .forEach { location ->
                    locationApi
                        .postLocation(
                            location = location,
                            deviceName = deviceName,
                            baseUrl = baseUrl,
                        )
                        .onSuccess { uploaded.add(location) }
                        .onFailure { exception ->
                            logW(TAG) { "failed to upload: $exception" }
                            uploaded.let { locationDao.delete(it) }
                            return Result.failure(exception)
                        }
                }
            locationDao.delete(uploaded)
        }
    }

    suspend fun get(from: Instant, to: Instant): List<Location> {
        val baseUrl = settings.getBaseUrl()
        val deviceName = settings.getDeviceName()
        locationApi.getLocations(
            deviceName = deviceName,
            baseUrl = baseUrl,
            fromDate = from,
            toDate = to,
        ).onSuccess { locations ->
            return locations.filter { it.accuracy <= Constants.LOCATION_MIN_HORIZONTAL_ACCURACY }
        }
        return emptyList()
    }

    fun getCount(): Flow<Int> {
        return locationDao
            .getAllFlow()
            .map { it.size }
            .distinctUntilChanged()
    }

    companion object {
        private const val TAG = "LocationRepository"
    }
}
