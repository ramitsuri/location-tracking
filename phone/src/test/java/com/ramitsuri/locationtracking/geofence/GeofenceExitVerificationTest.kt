package com.ramitsuri.locationtracking.geofence

import android.location.Location as AndroidLocation
import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.utils.toBd
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class GeofenceExitVerificationTest {

    private val receiver = GeofenceBroadcastReceiver()

    private val center = LatLng(37.7749.toBd(), (-122.4194).toBd())
    private val region = Region(
        id = 1,
        name = "Home",
        center = center,
        radiusMeters = 100f,
    )

    @Before
    fun setUp() {
        mockkStatic(AndroidLocation::class)
        every {
            AndroidLocation.distanceBetween(
                any(), any(), any(), any(), any()
            )
        } answers {
            val startLat = arg<Double>(0)
            val startLng = arg<Double>(1)
            val endLat = arg<Double>(2)
            val endLng = arg<Double>(3)
            val results = arg<FloatArray>(4)
            results[0] = computeHaversineDistanceMeters(startLat, startLng, endLat, endLng)
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(AndroidLocation::class)
    }

    @Test
    fun `evaluateExit - confirmed when distance minus accuracy exceeds radius`() {
        // Distance ~200m away (lat 37.7767, lng -122.4194 is ~200m north). Accuracy = 20m.
        // 200m - 20m = 180m > 100m -> Confirmed.
        val location = location(lat = 37.7767, lon = -122.4194, accuracy = 20)

        val verdict = receiver.evaluateExit(region, location)
        assertTrue(verdict is GeofenceBroadcastReceiver.ExitVerdict.Confirmed)
    }

    @Test
    fun `evaluateExit - rejected when distance plus accuracy is within radius`() {
        // Distance ~30m away (lat 37.77517, lng -122.4194 is ~30m north). Accuracy = 20m.
        // 30m + 20m = 50m <= 100m -> FalseExit.
        val location = location(lat = 37.77517, lon = -122.4194, accuracy = 20)

        val verdict = receiver.evaluateExit(region, location)
        assertTrue(verdict is GeofenceBroadcastReceiver.ExitVerdict.FalseExit)
    }

    @Test
    fun `evaluateExit - inconclusive when accuracy straddles boundary`() {
        // Distance ~110m away. Accuracy = 30m.
        // 110 - 30 = 80 <= 100 (not confirmed)
        // 110 + 30 = 140 > 100 (not rejected) -> Inconclusive.
        val location = location(lat = 37.77589, lon = -122.4194, accuracy = 30)

        val verdict = receiver.evaluateExit(region, location)
        assertTrue(verdict is GeofenceBroadcastReceiver.ExitVerdict.Inconclusive)
    }

    @Test
    fun `evaluateExit - unverified when region is null`() {
        val location = location(lat = 37.7767, lon = -122.4194, accuracy = 20)
        val verdict = receiver.evaluateExit(null, location)
        assertTrue(verdict is GeofenceBroadcastReceiver.ExitVerdict.Unverified)
        assertEquals("region not found", verdict.reason)
    }

    @Test
    fun `evaluateExit - unverified when location is null`() {
        val verdict = receiver.evaluateExit(region, null)
        assertTrue(verdict is GeofenceBroadcastReceiver.ExitVerdict.Unverified)
        assertEquals("no fresh location available", verdict.reason)
    }

    private fun location(lat: Double, lon: Double, accuracy: Int): Location {
        return Location(
            latitude = lat,
            longitude = lon,
            altitude = 0,
            accuracy = accuracy,
            verticalAccuracy = 0,
            bearing = 0,
            locationTimestamp = Instant.parse("2025-01-01T10:00:00Z"),
            velocity = 0,
        )
    }

    private fun computeHaversineDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
    ): Float {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toFloat()
    }
}
