package com.ramitsuri.locationtracking.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.JointType
import com.google.maps.android.compose.GoogleMapComposable
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.ui.region.toAndroidLatLng

@GoogleMapComposable
@Composable
fun Polygon(
    region: Region,
    onRegionClick: ((Region) -> Unit)? = null,
) {
    com.google.maps.android.compose.Polygon(
        points = region.latLngs.map { it.toAndroidLatLng() },
        fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        strokeColor = MaterialTheme.colorScheme.secondary,
        strokeJointType = JointType.ROUND,
        strokeWidth = 20f,
        clickable = onRegionClick != null,
        onClick = { onRegionClick?.invoke(region) },
    )
}
