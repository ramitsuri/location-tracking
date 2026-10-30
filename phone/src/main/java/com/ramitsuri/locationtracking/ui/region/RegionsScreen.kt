package com.ramitsuri.locationtracking.ui.region

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.ramitsuri.locationtracking.model.GeofenceEvent
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.ui.components.Map
import com.ramitsuri.locationtracking.ui.components.RegionCircle
import com.ramitsuri.locationtracking.ui.components.TintedIconButton
import com.ramitsuri.locationtracking.utils.format
import kotlin.math.roundToInt
import kotlinx.datetime.TimeZone

@Composable
fun RegionsScreen(
    viewState: RegionsViewState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onRegionLatLng: (AndroidLatLng) -> Unit,
    onRegionRadiusChanged: (Float) -> Unit,
    onRegionNameChanged: (String) -> Unit,
    onClearUnsavedRegion: () -> Unit,
    onSaveRegion: () -> Unit,
    onUpdateRegion: (Region) -> Unit,
    onDeleteRegion: (Region) -> Unit,
) {
    ScreenContent(
        viewState = viewState,
        modifier = modifier,
        onLatLngReceived = onRegionLatLng,
        onRadiusChanged = onRegionRadiusChanged,
        onSaveRegion = onSaveRegion,
        onClearUnsavedRegion = onClearUnsavedRegion,
        onBack = onBack,
        onRegionNameChanged = onRegionNameChanged,
        onUpdateRegion = onUpdateRegion,
        onDeleteRegion = onDeleteRegion,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    viewState: RegionsViewState,
    modifier: Modifier = Modifier,
    onLatLngReceived: (AndroidLatLng) -> Unit,
    onRadiusChanged: (Float) -> Unit,
    onSaveRegion: () -> Unit,
    onClearUnsavedRegion: () -> Unit,
    onBack: () -> Unit,
    onRegionNameChanged: (String) -> Unit,
    onUpdateRegion: (Region) -> Unit,
    onDeleteRegion: (Region) -> Unit,
) {
    val regionNameSheetState = rememberModalBottomSheetState()
    var showRegionNameSheet by remember { mutableStateOf(false) }

    val regionsSheetState = rememberModalBottomSheetState()
    var showRegionsSheet by remember { mutableStateOf(false) }

    val geofenceEventsSheetState = rememberModalBottomSheetState()
    var showGeofenceEventsSheet by remember { mutableStateOf(false) }

    val clickedRegionInfoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var clickedRegionForInfo by remember { mutableStateOf<Region?>(null) }
    var clickedRegionForMapCentering by remember { mutableStateOf<Region?>(null) }

    var editingRegionRadius by remember(clickedRegionForInfo) {
        mutableFloatStateOf(clickedRegionForInfo?.radiusMeters ?: Region.DEFAULT_RADIUS_METERS)
    }
    var editingRegionName by remember(clickedRegionForInfo) {
        mutableStateOf(clickedRegionForInfo?.name.orEmpty())
    }

    var deleteRegion by remember { mutableStateOf<Region?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        MapContent(
            viewState = viewState,
            editingRegionId = clickedRegionForInfo?.id,
            editingRegionRadius = editingRegionRadius,
            regionForCentering = clickedRegionForMapCentering,
            onResetRegionForCentering = { clickedRegionForMapCentering = null },
            onMapClick = onLatLngReceived,
            onRegionClick = { clickedRegionForInfo = it },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .displayCutoutPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopRow(
                showClearUnsavedRegionButton = !viewState.isUnsavedRegionEmpty,
                showSaveUnsavedRegionButton = viewState.canSaveRegion,
                showRegionsButton = viewState.regions.isNotEmpty(),
                showRequestUnsavedRegionNameButton = viewState.unsavedRegionNeedsName,
                onRequestUnsavedRegionName = { showRegionNameSheet = true },
                onSavedRegionsButtonClick = { showRegionsSheet = true },
                onGeofenceEventsButtonClick = { showGeofenceEventsSheet = true },
                onSaveRegion = onSaveRegion,
                onClearRegion = onClearUnsavedRegion,
                onBack = onBack,
            )
        }

        viewState.unsavedRegion?.let { unsavedRegion ->
            RadiusControl(
                radiusMeters = unsavedRegion.radiusMeters,
                onRadiusChanged = onRadiusChanged,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
            )
        }

        if (showRegionNameSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRegionNameSheet = false },
                sheetState = regionNameSheetState,
            ) {
                RegionNameSheetContent(
                    name = viewState.unsavedRegion?.name.orEmpty(),
                    onNameChanged = onRegionNameChanged,
                    onSave = {
                        onSaveRegion()
                        showRegionNameSheet = false
                    },
                    onCancel = {
                        onClearUnsavedRegion()
                        showRegionNameSheet = false
                    },
                )
            }
        }

        if (showRegionsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRegionsSheet = false },
                sheetState = regionsSheetState,
            ) {
                ManageRegionsSheetContent(
                    regions = viewState.regions,
                    onRegionClick = {
                        clickedRegionForMapCentering = it
                        clickedRegionForInfo = it
                        showRegionsSheet = false
                    },
                    onDeleteClick = {
                        deleteRegion = it
                        showRegionsSheet = false
                    },
                )
            }
        }

        if (showGeofenceEventsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGeofenceEventsSheet = false },
                sheetState = geofenceEventsSheetState,
            ) {
                GeofenceEventsSheetContent(
                    events = viewState.geofenceEvents,
                    timeZone = viewState.timeZone,
                )
            }
        }

        clickedRegionForInfo?.let { region ->
            ModalBottomSheet(
                onDismissRequest = { clickedRegionForInfo = null },
                sheetState = clickedRegionInfoSheetState,
            ) {
                RegionInfoSheetContent(
                    radiusMeters = editingRegionRadius,
                    name = editingRegionName,
                    onRadiusChanged = { editingRegionRadius = it },
                    onNameChanged = { editingRegionName = it },
                    onSaveRegion = {
                        onUpdateRegion(region.copy(name = editingRegionName.trim(), radiusMeters = editingRegionRadius))
                        clickedRegionForInfo = null
                    },
                    onDeleteRegion = {
                        deleteRegion = region
                        clickedRegionForInfo = null
                    },
                    onCancel = { clickedRegionForInfo = null },
                )
            }
        }

        deleteRegion?.let { region ->
            AlertDialog(
                onDismissRequest = { deleteRegion = null },
                title = { Text(stringResource(R.string.delete_region)) },
                text = { Text(region.name) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteRegion(region)
                            deleteRegion = null
                        },
                    ) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    Button(onClick = { deleteRegion = null }) {
                        Text(stringResource(R.string.no))
                    }
                },
            )
        }
    }
}

@Composable
private fun MapContent(
    viewState: RegionsViewState,
    editingRegionId: Int?,
    editingRegionRadius: Float,
    regionForCentering: Region?,
    onResetRegionForCentering: () -> Unit,
    onMapClick: (AndroidLatLng) -> Unit,
    onRegionClick: (Region) -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            viewState.initialMapCenter.toAndroidLatLng(),
            13f,
        )
    }
    var zoom by remember { mutableFloatStateOf(cameraPositionState.position.zoom) }
    LaunchedEffect(cameraPositionState.position.zoom) {
        zoom = cameraPositionState.position.zoom
    }
    LaunchedEffect(regionForCentering) {
        if (regionForCentering != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                regionForCentering.center.toAndroidLatLng(),
                zoom,
            )
            onResetRegionForCentering()
        }
    }

    Map(
        cameraPositionState = cameraPositionState,
        onMapClick = onMapClick,
    ) {
        val lastKnownLocation = viewState.lastKnownLocation
        if (lastKnownLocation != null) {
            val markerState = rememberUpdatedMarkerState(
                position = lastKnownLocation.toAndroidLatLng(),
            )
            Marker(
                state = markerState,
                title = stringResource(R.string.last_known_location),
            )
        }

        val unsavedRegion = viewState.unsavedRegion
        if (unsavedRegion == null) {
            viewState.regions.forEach { region ->
                val radius = if (region.id == editingRegionId) {
                    editingRegionRadius
                } else {
                    region.radiusMeters
                }
                RegionCircle(
                    region = region.copy(radiusMeters = radius),
                    onRegionClick = onRegionClick,
                )
            }
        } else {
            RegionCircle(
                center = unsavedRegion.center,
                radiusMeters = unsavedRegion.radiusMeters,
            )
        }
    }
}

@Composable
private fun RegionInfoSheetContent(
    radiusMeters: Float,
    name: String,
    onRadiusChanged: (Float) -> Unit,
    onNameChanged: (String) -> Unit,
    onSaveRegion: () -> Unit,
    onDeleteRegion: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.edit_region),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.region_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        RadiusControl(
            radiusMeters = radiusMeters,
            onRadiusChanged = onRadiusChanged,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDeleteRegion) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            Row {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = onSaveRegion,
                    enabled = name.isNotBlank(),
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
private fun RadiusControl(
    radiusMeters: Float,
    onRadiusChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.region_radius, radiusMeters.roundToInt()),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = radiusMeters,
            onValueChange = onRadiusChanged,
            valueRange = Region.MIN_RADIUS_METERS..Region.MAX_RADIUS_METERS,
        )
    }
}

@Composable
private fun ManageRegionsSheetContent(
    regions: List<Region>,
    onRegionClick: (Region) -> Unit,
    onDeleteClick: (Region) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(regions, key = { it.id }) { region ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRegionClick(region) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = region.name,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = stringResource(R.string.region_radius, region.radiusMeters.roundToInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { onRegionClick(region) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                        )
                    }
                    IconButton(onClick = { onDeleteClick(region) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopRow(
    showClearUnsavedRegionButton: Boolean,
    showSaveUnsavedRegionButton: Boolean,
    showRegionsButton: Boolean,
    showRequestUnsavedRegionNameButton: Boolean,
    onBack: () -> Unit,
    onRequestUnsavedRegionName: () -> Unit,
    onSavedRegionsButtonClick: () -> Unit,
    onGeofenceEventsButtonClick: () -> Unit,
    onSaveRegion: () -> Unit,
    onClearRegion: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TintedIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        if (showClearUnsavedRegionButton ||
            showSaveUnsavedRegionButton ||
            showRegionsButton ||
            showRequestUnsavedRegionNameButton
        ) {
            PillContent(
                showClearUnsavedRegionButton = showClearUnsavedRegionButton,
                showSaveUnsavedRegionButton = showSaveUnsavedRegionButton,
                showRegionsButton = showRegionsButton,
                showRequestUnsavedRegionNameButton = showRequestUnsavedRegionNameButton,
                onRequestUnsavedRegionName = onRequestUnsavedRegionName,
                onSavedRegionsButtonClick = onSavedRegionsButtonClick,
                onGeofenceEventsButtonClick = onGeofenceEventsButtonClick,
                onSaveRegion = onSaveRegion,
                onClearRegion = onClearRegion,
            )
        }
    }
}

@Composable
private fun PillContent(
    showClearUnsavedRegionButton: Boolean,
    showSaveUnsavedRegionButton: Boolean,
    showRegionsButton: Boolean,
    showRequestUnsavedRegionNameButton: Boolean,
    onRequestUnsavedRegionName: () -> Unit,
    onSavedRegionsButtonClick: () -> Unit,
    onGeofenceEventsButtonClick: () -> Unit,
    onSaveRegion: () -> Unit,
    onClearRegion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (showClearUnsavedRegionButton ||
            showSaveUnsavedRegionButton ||
            showRequestUnsavedRegionNameButton
        ) {
            if (showSaveUnsavedRegionButton) {
                TintedIconButton(
                    onClick = onSaveRegion,
                    icon = Icons.Outlined.Done,
                )
            }
            if (showRequestUnsavedRegionNameButton) {
                TintedIconButton(
                    onClick = onRequestUnsavedRegionName,
                    icon = Icons.Outlined.Done,
                )
            }
            if (showClearUnsavedRegionButton) {
                TintedIconButton(
                    onClick = onClearRegion,
                    icon = Icons.Filled.Clear,
                )
            }
        } else {
            if (showRegionsButton) {
                TintedIconButton(
                    onClick = onSavedRegionsButtonClick,
                    icon = Icons.AutoMirrored.Filled.List,
                )
                TintedIconButton(
                    onClick = onGeofenceEventsButtonClick,
                    icon = Icons.Outlined.History,
                )
            }
        }
    }
}

@Composable
private fun GeofenceEventsSheetContent(
    events: List<GeofenceEvent>,
    timeZone: TimeZone,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.geofence_events),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        if (events.isEmpty()) {
            Text(
                text = stringResource(R.string.no_geofence_events),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(events, key = { it.id }) { event ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = event.regionName,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            TransitionBadge(transition = event.transition)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = event.time.format(
                                    timeZone = timeZone,
                                    am = stringResource(R.string.am),
                                    pm = stringResource(R.string.pm),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "%.4f, %.4f".format(event.latitude, event.longitude),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitionBadge(
    transition: String,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor, label) = when (transition.uppercase()) {
        "ENTER" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            stringResource(R.string.geofence_transition_enter),
        )
        "EXIT" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            stringResource(R.string.geofence_transition_exit),
        )
        "DWELL" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            stringResource(R.string.geofence_transition_dwell),
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.onSurface,
            transition,
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}

@Composable
private fun RegionNameSheetContent(
    name: String,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.region_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = onSave,
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    }
}
