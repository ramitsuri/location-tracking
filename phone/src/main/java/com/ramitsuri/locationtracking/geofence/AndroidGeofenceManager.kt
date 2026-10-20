package com.ramitsuri.locationtracking.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.model.Region
import kotlin.time.Duration.Companion.minutes

class AndroidGeofenceManager(
    private val context: Context,
    private val regionDao: RegionDao,
) {
    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    @SuppressLint("MissingPermission")
    suspend fun syncGeofences() {
        val regions = regionDao.getAll()
        logI(TAG) { "syncGeofences: found ${regions.size} regions" }

        geofencingClient.removeGeofences(geofencePendingIntent).addOnCompleteListener {
            if (regions.isEmpty()) {
                logI(TAG) { "No regions to add to GeofencingClient" }
                return@addOnCompleteListener
            }

            val geofences = regions.map { region ->
                createGeofenceFromRegion(region)
            }

            val geofencingRequest = GeofencingRequest.Builder()
                // Use DWELL so re-registering fences doesn't fire spurious ENTERs from a coarse fix
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(geofences)
                .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    logI(TAG) { "Successfully added ${geofences.size} geofences" }
                }
                .addOnFailureListener { e ->
                    logE(TAG, e) { "Failed to add geofences" }
                }
        }
    }

    private fun createGeofenceFromRegion(region: Region): Geofence {
        val latitude = region.center.latitude.toDouble()
        val longitude = region.center.longitude.toDouble()
        val radiusMeters = region.radiusMeters.coerceAtLeast(Region.MIN_RADIUS_METERS)

        logI(TAG) {
            "Creating geofence for region '${region.name}' (id=${region.id}): " +
                "center=($latitude, $longitude), radius=${radiusMeters}m"
        }

        return Geofence.Builder()
            .setRequestId(region.id.toString())
            .setCircularRegion(latitude, longitude, radiusMeters)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_EXIT or
                    Geofence.GEOFENCE_TRANSITION_DWELL,
            )
            .setLoiteringDelay(LOITERING_DELAY.inWholeMilliseconds.toInt())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    companion object {
        private const val TAG = "GeofenceManager"
        private val LOITERING_DELAY = 3.minutes
    }
}
