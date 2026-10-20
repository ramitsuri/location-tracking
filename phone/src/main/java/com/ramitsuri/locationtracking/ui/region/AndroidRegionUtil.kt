package com.ramitsuri.locationtracking.ui.region

import android.location.Location as AndroidLocation
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.utils.RegionUtil
import com.ramitsuri.locationtracking.utils.toBd

class AndroidRegionUtil : RegionUtil {
    override fun contains(region: Region, location: Location): Boolean {
        val results = FloatArray(1)
        AndroidLocation.distanceBetween(
            region.center.latitude.toDouble(),
            region.center.longitude.toDouble(),
            location.latitude,
            location.longitude,
            results,
        )
        return results[0] <= region.radiusMeters
    }
}

fun AndroidLatLng.toLatLng() = LatLng(latitude.toBd(), longitude.toBd())

fun LatLng.toAndroidLatLng() = AndroidLatLng(latitude.toDouble(), longitude.toDouble())

fun Location.toAndroidLatLng() = AndroidLatLng(latitude, longitude)

fun Location.toLatLng() = LatLng(latitude.toBd(), longitude.toBd())
