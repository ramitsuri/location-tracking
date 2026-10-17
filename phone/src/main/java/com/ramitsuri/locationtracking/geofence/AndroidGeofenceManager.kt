package com.ramitsuri.locationtracking.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location as AndroidLocation
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.model.Region

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

            val geofences = regions.mapNotNull { region ->
                createGeofenceFromRegion(region)
            }

            if (geofences.isEmpty()) {
                logI(TAG) { "No valid geofences created from regions" }
                return@addOnCompleteListener
            }

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
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

    private fun createGeofenceFromRegion(region: Region): Geofence? {
        if (region.latLngs.isEmpty()) return null

        val avgLat = region.latLngs.map { it.latitude.toDouble() }.average()
        val avgLng = region.latLngs.map { it.longitude.toDouble() }.average()

        var maxDistanceMeters = 0.0f
        val results = FloatArray(1)
        region.latLngs.forEach { point ->
            AndroidLocation.distanceBetween(
                avgLat,
                avgLng,
                point.latitude.toDouble(),
                point.longitude.toDouble(),
                results,
            )
            if (results[0] > maxDistanceMeters) {
                maxDistanceMeters = results[0]
            }
        }

        val radiusMeters = maxOf(100.0f, maxDistanceMeters + 50.0f)

        logI(TAG) {
            "Creating geofence for region '${region.name}' (id=${region.id}): " +
                "center=($avgLat, $avgLng), radius=${radiusMeters}m"
        }

        return Geofence.Builder()
            .setRequestId(region.id.toString())
            .setCircularRegion(avgLat, avgLng, radiusMeters)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT,
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    companion object {
        private const val TAG = "GeofenceManager"
    }
}
