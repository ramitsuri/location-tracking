package com.ramitsuri.locationtracking.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMapComposable
import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.ui.region.toAndroidLatLng

@GoogleMapComposable
@Composable
fun RegionCircle(
    region: Region,
    onRegionClick: ((Region) -> Unit)? = null,
) {
    RegionCircle(
        center = region.center,
        radiusMeters = region.radiusMeters,
        onClick = onRegionClick?.let { { it(region) } },
    )
}

@GoogleMapComposable
@Composable
fun RegionCircle(
    center: LatLng,
    radiusMeters: Float,
    onClick: (() -> Unit)? = null,
) {
    Circle(
        center = center.toAndroidLatLng(),
        radius = radiusMeters.toDouble(),
        fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        strokeColor = MaterialTheme.colorScheme.secondary,
        strokeWidth = 10f,
        clickable = onClick != null,
        onClick = { onClick?.invoke() },
    )
}
