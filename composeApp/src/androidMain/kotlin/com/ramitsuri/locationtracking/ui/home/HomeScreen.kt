package com.ramitsuri.locationtracking.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun HomeScreen(
    viewState: HomeViewState,
    modifier: Modifier = Modifier,
    onNavToSettings: () -> Unit,
    onNavToSystemSettings: () -> Unit,
    onSingleLocation: () -> Unit,
    onUpload: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        val markerState = rememberMarkerState()
        val cameraPositionState = rememberCameraPositionState()
        LaunchedEffect(viewState.lastKnownLocation) {
            viewState.lastKnownLocation?.let { lastKnownLocation ->
                markerState.position =
                    LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(markerState.position, 13f)
            }
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
            uiSettings = MapUiSettings(mapToolbarEnabled = false, zoomControlsEnabled = false),
        ) {
            Marker(state = markerState)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .displayCutoutPadding(),
            horizontalAlignment = Alignment.End,
        ) {
            val permissionsNotGranted = viewState.notGrantedPermissions.isNotEmpty()
            var isExpanded by remember(
                permissionsNotGranted,
            ) { mutableStateOf(permissionsNotGranted) }
            ExpandingPill(
                isExpanded = isExpanded,
                onExpandOrCollapse = { isExpanded = !isExpanded },
            ) {
                if (permissionsNotGranted) {
                    TintedIconButton(
                        onClick = onNavToSystemSettings,
                        icon = Icons.Default.Warning,
                    )
                } else {
                    if (viewState.numOfLocations > 0) {
                        TintedButton(onClick = { }) {
                            Text(
                                text = viewState.numOfLocations.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                    if (viewState.isUploadWorkerRunning) {
                        TintedButton(onClick = {}) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    } else {
                        TintedIconButton(
                            onClick = onUpload,
                            icon = Icons.Default.Upload,
                        )
                    }
                    TintedIconButton(
                        onClick = onSingleLocation,
                        icon = Icons.Default.AddLocation,
                    )
                }
                TintedIconButton(
                    onClick = onNavToSettings,
                    icon = Icons.Default.Settings,
                )
            }
        }
    }
}

@Composable
private fun ExpandingPill(
    isExpanded: Boolean,
    onExpandOrCollapse: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val buttonRotate by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "buttonRotate",
    )
    Row(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
    ) {
        AnimatedVisibility(visible = isExpanded) {
            Row {
                content()
            }
        }
        TintedIconButton(
            onClick = onExpandOrCollapse,
            icon = Icons.Default.ChevronLeft,
            modifier = Modifier.rotate(buttonRotate),
        )
    }
}

@Composable
private fun TintedIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun TintedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        content()
    }
}
