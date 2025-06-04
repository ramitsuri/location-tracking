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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.rememberCameraPositionState
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.ui.components.Map
import com.ramitsuri.locationtracking.ui.components.Polygon
import com.ramitsuri.locationtracking.ui.components.TintedIconButton
import com.ramitsuri.locationtracking.utils.center

@Composable
fun RegionsScreen(
    viewState: RegionsViewState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onRegionLatLng: (AndroidLatLng) -> Unit,
    onRegionNameChanged: (String) -> Unit,
    onClearUnsavedRegion: () -> Unit,
    onSaveRegion: () -> Unit,
    onDeleteRegion: (Region) -> Unit,
) {
    ScreenContent(
        viewState = viewState,
        modifier = modifier,
        onLatLngReceived = onRegionLatLng,
        onSaveRegion = onSaveRegion,
        onClearUnsavedRegion = onClearUnsavedRegion,
        onBack = onBack,
        onRegionNameChanged = onRegionNameChanged,
        onDeleteRegion = onDeleteRegion,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    viewState: RegionsViewState,
    modifier: Modifier = Modifier,
    onLatLngReceived: (AndroidLatLng) -> Unit,
    onSaveRegion: () -> Unit,
    onClearUnsavedRegion: () -> Unit,
    onBack: () -> Unit,
    onRegionNameChanged: (String) -> Unit,
    onDeleteRegion: (Region) -> Unit,
) {
    val regionNameSheetState = rememberModalBottomSheetState()
    var showRegionNameSheet by remember { mutableStateOf(false) }

    val regionsSheetState = rememberModalBottomSheetState()
    var showRegionsSheet by remember { mutableStateOf(false) }

    val clickedRegionInfoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var clickedRegionForInfo by remember { mutableStateOf<Region?>(null) }
    var clickedRegionForMapCentering by remember { mutableStateOf<Region?>(null) }

    var deleteRegion by remember { mutableStateOf<Region?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        MapContent(
            viewState = viewState,
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
                onSaveRegion = onSaveRegion,
                onClearRegion = onClearUnsavedRegion,
                onBack = onBack,
            )
        }

        if (showRegionNameSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRegionNameSheet = false },
                sheetState = regionNameSheetState,
            ) {
                RegionNameSheetContent(
                    name = viewState.unsavedRegion.name,
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
                        // TODO Center map on clicked region
                        clickedRegionForMapCentering = it
                        showRegionsSheet = false
                    },
                    onDeleteClick = {
                        deleteRegion = it
                        showRegionsSheet = false
                    },
                )
            }
        }

        clickedRegionForInfo?.let { region ->
            ModalBottomSheet(
                onDismissRequest = { clickedRegionForInfo = null },
                sheetState = clickedRegionInfoSheetState,
            ) {
                RegionInfoSheetContent(region = region)
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
            val center = regionForCentering.latLngs.center()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(center, zoom)
            onResetRegionForCentering()
        }
    }

    Map(
        cameraPositionState = cameraPositionState,
        onMapClick = onMapClick,
    ) {
        if (viewState.isUnsavedRegionEmpty) {
            viewState.regions.forEach { region ->
                Polygon(
                    region = region,
                    onRegionClick = onRegionClick
                )
            }
        } else if (viewState.unsavedRegion.latLngs.size == 1) {
            Circle(
                center = viewState.unsavedRegion.latLngs.first().toAndroidLatLng(),
                clickable = false,
                fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                strokeColor = MaterialTheme.colorScheme.secondary,
                radius = 0.5,
                strokeWidth = 20f,
            )
        } else {
            Polygon(region = viewState.unsavedRegion)
        }
    }
}

@Composable
private fun RegionInfoSheetContent(
    region: Region,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(region.name)
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
            items(regions) { region ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRegionClick(region) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = region.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
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
            }
        }
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
