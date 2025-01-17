package com.ramitsuri.locationtracking.tracking.location

import android.content.Context
import android.location.Location as AndroidLocation
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity.GRANULARITY_PERMISSION_LEVEL
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest as GmsRequest
import com.google.android.gms.location.LocationRequest.Builder as GmsRequestBuilder
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority as GmsPriority
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.model.Location
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.datetime.Instant

class AndroidLocationProvider(context: Context) : LocationProvider {
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @RequiresPermission(
        anyOf = [
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
        ],
    )
    override fun requestUpdates(request: Request): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (androidLocation in result.locations) {
                    try {
                        trySend(androidLocation.asLocation())
                    } catch (_: Throwable) {
                    }
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            request.asGmsRequest(),
            callback,
            Looper.getMainLooper(),
        ).addOnFailureListener { e ->
            close(e)
        }

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(callback)
        }
    }

    @RequiresPermission(
        anyOf = [
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
        ],
    )
    override suspend fun requestSingle() = suspendCoroutine { continuation ->
        fusedLocationProviderClient
            .getCurrentLocation(
                CurrentLocationRequest.Builder()
                    .setMaxUpdateAgeMillis(5.seconds.inWholeMilliseconds)
                    .setGranularity(GRANULARITY_PERMISSION_LEVEL)
                    .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                    .build(),
                null,
            )
            .addOnSuccessListener { location: AndroidLocation? ->
                // Turns out location can be null! Especially if there's no location available or if
                // locations are turned off on the device.
                if (location == null) {
                    logW("LocationProvider") {
                        "No location available for single high accuracy request. Device location " +
                            "disabled?"
                    }
                }
                continuation.resume(location?.asLocation())
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    private fun Request.asGmsRequest(): GmsRequest {
        val gmsPriority =
            when (priority) {
                Priority.HighAccuracy -> GmsPriority.PRIORITY_HIGH_ACCURACY
                Priority.BalancedPowerAccuracy -> GmsPriority.PRIORITY_BALANCED_POWER_ACCURACY
                Priority.LowPower -> GmsPriority.PRIORITY_LOW_POWER
            }

        return GmsRequestBuilder(
            /* priority = */
            gmsPriority,
            /* intervalMillis = */
            interval.inWholeMilliseconds,
        ).apply {
            minUpdateDistanceMeters?.let { setMinUpdateDistanceMeters(it) }
            fastestInterval?.let { setMinUpdateIntervalMillis(it.inWholeMilliseconds) }
        }.build()
    }

    private fun AndroidLocation.asLocation() = Location(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude.roundToInt(),
        accuracy = accuracy.roundToInt(),
        verticalAccuracy =
        if (hasVerticalAccuracy()) {
            verticalAccuracyMeters.toInt()
        } else {
            0
        },
        bearing = bearing.roundToInt(),
        locationTimestamp = Instant.fromEpochMilliseconds(time),
        // Convert m/s to km/h
        velocity = if (hasSpeed()) ((speed * 3.6).toInt()) else 0,
    )
}
