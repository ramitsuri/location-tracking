package com.ramitsuri.locationtracking.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapUiSettings

@Composable
fun Map(
    cameraPositionState: CameraPositionState,
    onMapClick: (AndroidLatLng) -> Unit = {},
    content:
    @Composable @GoogleMapComposable
        () -> Unit = {},
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        uiSettings = MapUiSettings(mapToolbarEnabled = false, zoomControlsEnabled = false),
        onMapClick = onMapClick,
    ) {
        content()
    }
}
