package com.ramitsuri.locationtracking.geofence

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location as AndroidLocation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val regionDao: RegionDao by inject()
    private val scope: CoroutineScope by inject()
    private val locationProvider: LocationProvider by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            logE(TAG) { "Geofence event error code: ${event.errorCode}" }
            return
        }

        val transitionType = event.geofenceTransition
        val transitionName = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "DWELL"
            else -> "UNKNOWN ($transitionType)"
        }

        val triggeringGeofences = event.triggeringGeofences ?: emptyList()
        val location = event.triggeringLocation

        val pendingResult = goAsync()
        scope.launch {
            try {
                val allRegions = regionDao.getAll()
                val regions = triggeringGeofences.map { geofence ->
                    val regionId = geofence.requestId.toIntOrNull()
                    geofence.requestId to allRegions.firstOrNull { it.id == regionId }
                }

                regions.forEach { (requestId, region) ->
                    val regionName = region?.name ?: "Region ID $requestId"
                    val baseMessage =
                        "Geofence $transitionName for region '$regionName' at ($location)"

                    if (transitionType != Geofence.GEOFENCE_TRANSITION_EXIT) {
                        logI(TAG) { baseMessage }
                        return@forEach
                    }

                    processExitVerification(region, baseMessage)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processExitVerification(
        region: Region?,
        baseMessage: String,
    ) {
        var freshLocation = requestFreshLocation()
        var verdict = evaluateExit(region, freshLocation)

        if (verdict is ExitVerdict.Inconclusive) {
            val inc = verdict
            logI(TAG) {
                "$baseMessage - 1st fix inconclusive ${freshLocation.describe()} " +
                    "(dist=${inc.distance.toInt()}m, acc=${inc.accuracy}m, radius=${inc.radius.toInt()}m), retrying in 3s..."
            }
            delay(RETRY_DELAY)
            freshLocation = requestFreshLocation()
            verdict = evaluateExit(region, freshLocation)
        }

        val finalVerdict = verdict
        val finalLocation = freshLocation
        when (finalVerdict) {
            is ExitVerdict.Confirmed -> logI(TAG) {
                "$baseMessage - confirmed by fresh fix ${finalLocation.describe()} " +
                    "(dist=${finalVerdict.distance.toInt()}m - acc=${finalVerdict.accuracy}m > radius=${finalVerdict.radius.toInt()}m)"
            }

            is ExitVerdict.FalseExit -> logW(TAG) {
                "$baseMessage - ignored, fresh fix ${finalLocation.describe()} is inside region " +
                    "(dist=${finalVerdict.distance.toInt()}m + acc=${finalVerdict.accuracy}m <= radius=${finalVerdict.radius.toInt()}m)"
            }

            is ExitVerdict.Inconclusive -> logW(TAG) {
                "$baseMessage - inconclusive after retry: fresh fix ${finalLocation.describe()} " +
                    "(dist=${finalVerdict.distance.toInt()}m, acc=${finalVerdict.accuracy}m, radius=${finalVerdict.radius.toInt()}m)"
            }

            is ExitVerdict.Unverified -> logW(TAG) {
                "$baseMessage - unverified: ${finalVerdict.reason}"
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(): Location? {
        return withTimeoutOrNull(FRESH_LOCATION_TIMEOUT) { locationProvider.requestSingle() }
    }

    internal fun evaluateExit(region: Region?, freshLocation: Location?): ExitVerdict {
        if (region == null) return ExitVerdict.Unverified("region not found")
        if (freshLocation == null) return ExitVerdict.Unverified("no fresh location available")

        val results = FloatArray(1)
        AndroidLocation.distanceBetween(
            region.center.latitude.toDouble(),
            region.center.longitude.toDouble(),
            freshLocation.latitude,
            freshLocation.longitude,
            results,
        )
        val distance = results[0]
        val accuracy = freshLocation.accuracy
        val radius = region.radiusMeters

        return when {
            distance - accuracy > radius -> {
                ExitVerdict.Confirmed(distance, accuracy, radius)
            }

            distance + accuracy <= radius -> {
                ExitVerdict.FalseExit(distance, accuracy, radius)
            }

            else -> {
                ExitVerdict.Inconclusive(distance, accuracy, radius)
            }
        }
    }

    private fun Location?.describe(): String {
        return if (this == null) {
            "null"
        } else {
            "($latitude, $longitude, acc=${accuracy}m)"
        }
    }

    internal sealed interface ExitVerdict {
        data class Confirmed(val distance: Float, val accuracy: Int, val radius: Float) : ExitVerdict
        data class FalseExit(val distance: Float, val accuracy: Int, val radius: Float) : ExitVerdict
        data class Inconclusive(val distance: Float, val accuracy: Int, val radius: Float) : ExitVerdict
        data class Unverified(val reason: String) : ExitVerdict
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
        private val FRESH_LOCATION_TIMEOUT = 5.seconds
        private val RETRY_DELAY = 3.seconds
    }
}
