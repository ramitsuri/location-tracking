package com.ramitsuri.locationtracking.ui.region

import com.google.maps.android.PolyUtil
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.utils.RegionUtil
import com.ramitsuri.locationtracking.utils.toBd

class AndroidRegionUtil : RegionUtil {
    override fun contains(region: Region, location: Location): Boolean {
        val latLngs = region.latLngs.map {
            it.toAndroidLatLng()
        }
        return PolyUtil.containsLocation(
            /* latitude = */ location.latitude,
            /* longitude = */ location.longitude,
            /* polygon = */ latLngs,
            /* geodesic = */ true,
        )
    }
}

fun AndroidLatLng.toLatLng() = LatLng(latitude.toBd(), longitude.toBd())

fun LatLng.toAndroidLatLng() = AndroidLatLng(latitude.toDouble(), longitude.toDouble())

fun Location.toAndroidLatLng() = AndroidLatLng(latitude, longitude)

fun Location.toLatLng() = LatLng(latitude.toBd(), longitude.toBd())
