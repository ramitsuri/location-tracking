package com.ramitsuri.locationtracking.usecase

import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.geofence.AndroidGeofenceManager
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.ui.region.toLatLng
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AddTempRegionUseCase(
    private val locationProvider: LocationProvider,
    private val regionDao: RegionDao,
    private val geofenceManager: AndroidGeofenceManager,
    private val timeZone: TimeZone,
) {
    suspend operator fun invoke(): String? {
        val freshLocation = getAccurateLocationFix(maxAttempts = 3, targetAccuracyMeters = 50)
        if (freshLocation == null) {
            logW(TAG) { "Failed to get accurate location fix for temporary geofence" }
            return null
        }

        val tempName = generateTempRegionName()

        val tempRegion = Region(
            name = tempName,
            center = freshLocation.toLatLng(),
            radiusMeters = Region.DEFAULT_RADIUS_METERS,
            type = Region.Type.TEMP,
        )

        regionDao.insert(tempRegion)
        geofenceManager.syncGeofences()
        logI(TAG) { "Created temporary geofence '$tempName' at ($freshLocation)" }

        return tempName
    }

    private suspend fun getAccurateLocationFix(
        maxAttempts: Int = 3,
        targetAccuracyMeters: Int = 50,
    ): Location? {
        var bestFix: Location? = null
        for (attempt in 1..maxAttempts) {
            val fix = locationProvider.requestSingle()
            if (fix != null) {
                if (bestFix == null || fix.accuracy < bestFix.accuracy) {
                    bestFix = fix
                }
                if (fix.accuracy <= targetAccuracyMeters) {
                    return fix
                }
            }
            if (attempt < maxAttempts) {
                delay(1.seconds)
            }
        }
        return bestFix
    }

    @Suppress("DEPRECATION")
    private fun generateTempRegionName(): String {
        val now = Clock.System.now().toLocalDateTime(timeZone)
        val year = now.year.toString().padStart(4, '0')
        val month = now.monthNumber.toString().padStart(2, '0')
        val day = now.dayOfMonth.toString().padStart(2, '0')
        val hour = now.hour.toString().padStart(2, '0')
        val minute = now.minute.toString().padStart(2, '0')
        val second = now.second.toString().padStart(2, '0')
        return "temp_${year}${month}${day}_${hour}${minute}${second}"
    }

    companion object {
        private const val TAG = "AddTempRegionUseCase"
    }
}
