package com.ramitsuri.locationtracking.utils

import com.google.android.gms.maps.model.LatLngBounds
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.ui.region.toAndroidLatLng

@JvmName("latLngCenter")
fun List<LatLng>.center(): AndroidLatLng {
    return map { it.toAndroidLatLng() }.center()
}

@JvmName("androidLatLngCenter")
fun List<AndroidLatLng>.center(): AndroidLatLng {
    return LatLngBounds.Builder()
        .takeIf { isNotEmpty() }
        ?.apply { forEach { include(it) } }
        ?.build()
        ?.center
        ?: LatLng.NY.toAndroidLatLng()
}
