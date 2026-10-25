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
import com.ramitsuri.locationtracking.data.dao.GeofenceEventDao
import com.ramitsuri.locationtracking.model.GeofenceEvent
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.notification.NotificationManager
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
    private val geofenceEventDao: GeofenceEventDao by inject()
    private val scope: CoroutineScope by inject()
    private val locationProvider: LocationProvider by inject()
    private val notificationManager: NotificationManager by inject()
    private val geofenceManager: AndroidGeofenceManager by inject()

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
        val systemLocation = event.triggeringLocation

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
                        "Geofence $transitionName for region '$regionName' at ($systemLocation)"

                    if (transitionType != Geofence.GEOFENCE_TRANSITION_EXIT) {
                        logI(TAG) { baseMessage }
                        if (systemLocation != null) {
                            val geofenceEvent = GeofenceEvent(
                                regionName = regionName,
                                transition = transitionName,
                                latitude = systemLocation.latitude,
                                longitude = systemLocation.longitude,
                            )
                            geofenceEventDao.insert(geofenceEvent)
                            notificationManager.notifyGeofenceTrigger()
                        }
                        return@forEach
                    }

                    processExitVerification(
                        region = region,
                        regionName = regionName,
                        systemLocation = systemLocation,
                        baseMessage = baseMessage,
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processExitVerification(
        region: Region?,
        regionName: String,
        systemLocation: AndroidLocation?,
        baseMessage: String,
    ) {
        // First verify triggering system location if available
        if (systemLocation != null && region != null) {
            val systemVerdict = evaluateExit(region, systemLocation)
            if (systemVerdict is ExitVerdict.TooFarAway) {
                logW(TAG) {
                    "$baseMessage - ignored, triggering system location ${systemLocation.describe()} is too far away " +
                        "(dist=${systemVerdict.distance.toInt()}m > radius=${systemVerdict.radius.toInt()}m + buffer=${EXIT_BUFFER_METERS.toInt()}m + acc=${systemVerdict.accuracy}m)"
                }
                return
            }
        }

        var freshLocation = requestFreshLocation()
        var verdict = evaluateExit(region, freshLocation)

        if (verdict is ExitVerdict.InsideRegion) {
            val insideVerdict = verdict
            logI(TAG) {
                "$baseMessage - 1st fix inside region ${freshLocation.describe()} " +
                    "(dist=${insideVerdict.distance.toInt()}m + acc=${insideVerdict.accuracy}m <= radius=${insideVerdict.radius.toInt()}m), retrying in 3s..."
            }
            delay(RETRY_DELAY)
            freshLocation = requestFreshLocation()
            verdict = evaluateExit(region, freshLocation)
        }

        val finalVerdict = verdict
        val finalLocation = freshLocation
        val notifyLat = finalLocation?.latitude ?: systemLocation?.latitude
        val notifyLng = finalLocation?.longitude ?: systemLocation?.longitude

        when (finalVerdict) {
            is ExitVerdict.Confirmed -> {
                logI(TAG) {
                    "$baseMessage - confirmed by fresh fix ${finalLocation.describe()} " +
                        "(dist=${finalVerdict.distance.toInt()}m within radius=${finalVerdict.radius.toInt()}m + buffer=${EXIT_BUFFER_METERS.toInt()}m, acc=${finalVerdict.accuracy}m)"
                }
                if (notifyLat != null && notifyLng != null) {
                    val geofenceEvent = GeofenceEvent(
                        regionName = regionName,
                        transition = "EXIT",
                        latitude = notifyLat,
                        longitude = notifyLng,
                    )
                    geofenceEventDao.insert(geofenceEvent)
                    notificationManager.notifyGeofenceTrigger()
                }
                if (region != null && region.type == Region.Type.TEMP) {
                    regionDao.delete(region)
                    geofenceManager.syncGeofences()
                    logI(TAG) { "Removed temporary geofence '${region.name}' (id=${region.id}) on exit" }
                }
            }

            is ExitVerdict.InsideRegion -> {
                logW(TAG) {
                    "$baseMessage - ignored, fresh fix ${finalLocation.describe()} is inside region " +
                        "(dist=${finalVerdict.distance.toInt()}m + acc=${finalVerdict.accuracy}m <= radius=${finalVerdict.radius.toInt()}m)"
                }
            }

            is ExitVerdict.TooFarAway -> {
                logW(TAG) {
                    "$baseMessage - ignored, fresh fix ${finalLocation.describe()} is too far away " +
                        "(dist=${finalVerdict.distance.toInt()}m > radius=${finalVerdict.radius.toInt()}m + buffer=${EXIT_BUFFER_METERS.toInt()}m + acc=${finalVerdict.accuracy}m)"
                }
            }

            is ExitVerdict.Unverified -> {
                logW(TAG) {
                    "$baseMessage - unverified: ${finalVerdict.reason}"
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(): Location? {
        return withTimeoutOrNull(FRESH_LOCATION_TIMEOUT) { locationProvider.requestSingle() }
    }

    internal fun evaluateExit(region: Region?, location: Location?): ExitVerdict {
        if (region == null) return ExitVerdict.Unverified("region not found")
        if (location == null) return ExitVerdict.Unverified("no fresh location available")

        val results = FloatArray(1)
        AndroidLocation.distanceBetween(
            region.center.latitude.toDouble(),
            region.center.longitude.toDouble(),
            location.latitude,
            location.longitude,
            results,
        )
        val distance = results[0]
        val accuracy = location.accuracy
        val radius = region.radiusMeters

        return when {
            distance + accuracy <= radius -> {
                ExitVerdict.InsideRegion(distance, accuracy, radius)
            }

            distance - accuracy <= radius + EXIT_BUFFER_METERS -> {
                ExitVerdict.Confirmed(distance, accuracy, radius)
            }

            else -> {
                ExitVerdict.TooFarAway(distance, accuracy, radius)
            }
        }
    }

    internal fun evaluateExit(region: Region?, location: AndroidLocation?): ExitVerdict {
        if (region == null) return ExitVerdict.Unverified("region not found")
        if (location == null) return ExitVerdict.Unverified("no location available")

        val results = FloatArray(1)
        AndroidLocation.distanceBetween(
            region.center.latitude.toDouble(),
            region.center.longitude.toDouble(),
            location.latitude,
            location.longitude,
            results,
        )
        val distance = results[0]
        val accuracy = if (location.hasAccuracy()) location.accuracy.toInt() else 0
        val radius = region.radiusMeters

        return when {
            distance + accuracy <= radius -> {
                ExitVerdict.InsideRegion(distance, accuracy, radius)
            }

            distance - accuracy <= radius + EXIT_BUFFER_METERS -> {
                ExitVerdict.Confirmed(distance, accuracy, radius)
            }

            else -> {
                ExitVerdict.TooFarAway(distance, accuracy, radius)
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

    private fun AndroidLocation?.describe(): String {
        return if (this == null) {
            "null"
        } else {
            "($latitude, $longitude, acc=${if (hasAccuracy()) accuracy.toInt() else 0}m)"
        }
    }

    internal sealed interface ExitVerdict {
        data class Confirmed(val distance: Float, val accuracy: Int, val radius: Float) : ExitVerdict
        data class InsideRegion(val distance: Float, val accuracy: Int, val radius: Float) : ExitVerdict
        data class TooFarAway(val distance: Float, val accuracy: Int, val radius: Float) : ExitVerdict
        data class Unverified(val reason: String) : ExitVerdict
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
        private val FRESH_LOCATION_TIMEOUT = 5.seconds
        private val RETRY_DELAY = 3.seconds
        private const val EXIT_BUFFER_METERS = 100f
    }
}
