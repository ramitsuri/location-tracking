package com.ramitsuri.locationtracking.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AddLocation
import androidx.compose.material.icons.outlined.Battery4Bar
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.TripOrigin
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.BatteryStatus
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.ui.components.Date
import com.ramitsuri.locationtracking.ui.components.Loading
import com.ramitsuri.locationtracking.utils.format
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@Composable
fun HomeScreen(
    viewState: HomeViewState,
    modifier: Modifier = Modifier,
    onNavToSettings: () -> Unit,
    onNavToSystemSettings: () -> Unit,
    onSingleLocation: () -> Unit,
    onUpload: () -> Unit,
    onSelectDateForLocations: (LocalDate) -> Unit,
    onClearDate: () -> Unit,
    onLocationSelected: (Location) -> Unit,
    onClearSelectedLocation: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    when (viewState.viewMode) {
        is HomeViewState.ViewMode.LastKnownLocation -> {
            ScreenContent(
                modifier = modifier,
                timeZone = viewState.timeZone,
                selectedLocation = viewState.selectedLocation,
                mapContent = {
                    SingleLocationMap(viewState.viewMode.location, onLocationSelected)
                },
                pillContent = {
                    OptionsPill(
                        permissionsNotGranted = viewState.permissionsNotGranted,
                        numOfLocations = viewState.numOfLocations,
                        isUploadWorkerRunning = viewState.isUploadWorkerRunning,
                        onNavToSettings = onNavToSettings,
                        onNavToSystemSettings = onNavToSystemSettings,
                        onSingleLocation = onSingleLocation,
                        onUpload = onUpload,
                        onDatePickerClick = { showDatePicker = true },
                    )
                },
                onClearSelectedLocation = onClearSelectedLocation,
            )
        }

        is HomeViewState.ViewMode.LocationsForDate -> {
            var locationsViewMode by remember {
                mutableStateOf<LocationsViewMode>(
                    LocationsViewMode.Points,
                )
            }
            val mode = locationsViewMode
            ScreenContent(
                modifier = modifier,
                timeZone = viewState.timeZone,
                selectedLocation = viewState.selectedLocation,
                mapContent = {
                    LocationsForDateMap(
                        mode = mode,
                        locations = viewState.viewMode.locations,
                        onLocationClick = onLocationSelected,
                    )
                },
                pillContent = {
                    Row(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(
                                text = viewState.viewMode.date.format(),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        when (mode) {
                            is LocationsViewMode.Lines -> {
                                TintedIconButton(
                                    onClick = { locationsViewMode = LocationsViewMode.Motion },
                                    icon = Icons.Outlined.SlowMotionVideo,
                                )
                            }

                            is LocationsViewMode.Motion -> {
                                TintedIconButton(
                                    onClick = { locationsViewMode = LocationsViewMode.Points },
                                    icon = Icons.Outlined.TripOrigin,
                                )
                            }

                            is LocationsViewMode.Points -> {
                                TintedIconButton(
                                    onClick = { locationsViewMode = LocationsViewMode.Lines },
                                    icon = Icons.Outlined.Timeline,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TintedIconButton(
                            onClick = onClearDate,
                            icon = Icons.Outlined.Clear,
                        )
                    }
                },
                onClearSelectedLocation = onClearSelectedLocation,
            )
        }
    }
    if (showDatePicker) {
        Date(
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                onSelectDateForLocations(it)
                showDatePicker = false
            },
        )
    }
    if (viewState.isLoading) {
        Loading()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    mapContent: @Composable () -> Unit,
    pillContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    timeZone: TimeZone,
    selectedLocation: Location?,
    onClearSelectedLocation: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(true) }
    LaunchedEffect(selectedLocation) {
        if (selectedLocation != null) {
            showBottomSheet = true
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        mapContent()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .displayCutoutPadding(),
            horizontalAlignment = Alignment.End,
        ) {
            pillContent()
            Spacer(modifier = Modifier.weight(1f))
            selectedLocation?.let { location ->
                if (showBottomSheet) {
                    LocationDetail(sheetState, location, timeZone, onClearSelectedLocation)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDetail(
    sheetState: SheetState,
    location: Location,
    timeZone: TimeZone,
    onClearSelectedLocation: () -> Unit,
) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onClearSelectedLocation,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp),
        ) {
            LocationDetailRow(
                text = location.locationTimestamp.format(
                    timeZone = timeZone,
                    am = stringResource(R.string.am),
                    pm = stringResource(R.string.pm),
                ),
                icon = Icons.Outlined.AccessTime,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LocationDetailRow(
                text = stringResource(
                    R.string.location_detail_latitude_longitude,
                    location.latitude.toString(),
                    location.longitude.toString(),
                ),
                icon = Icons.Outlined.LocationOn,
            )
            location.battery?.let {
                Spacer(modifier = Modifier.height(8.dp))
                LocationDetailRow(
                    text = stringResource(
                        R.string.location_detail_battery,
                        location.battery.toString(),
                    ),
                    icon = when (location.batteryStatus) {
                        BatteryStatus.CHARGING -> Icons.Outlined.BatteryChargingFull
                        BatteryStatus.FULL -> Icons.Outlined.BatteryFull
                        else -> Icons.Outlined.Battery4Bar
                    },
                )
            }
            location.ssid?.let {
                Spacer(modifier = Modifier.height(8.dp))
                LocationDetailRow(
                    text = it,
                    icon = Icons.Outlined.Wifi,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocationDetailRow(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun SingleLocationMap(location: Location?, onLocationClick: (Location) -> Unit) {
    val markerState = rememberMarkerState()
    val cameraPositionState = rememberCameraPositionState()
    var zoom by remember { mutableFloatStateOf(13f) }
    LaunchedEffect(location) {
        location?.let {
            markerState.position =
                LatLng(it.latitude, it.longitude)
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(markerState.position, zoom)
        }
    }
    LaunchedEffect(cameraPositionState.position) {
        zoom = cameraPositionState.position.zoom
    }
    Map(
        cameraPositionState = cameraPositionState,
    ) {
        location?.let {
            Marker(
                state = markerState,
                onClick = { _ ->
                    onLocationClick(it)
                    true
                },
            )
        }
    }
}

@Composable
private fun LocationsForDateMap(
    mode: LocationsViewMode,
    locations: List<Location>,
    onLocationClick: (Location) -> Unit,
) {
    val latLngs = remember(locations) { locations.map { LatLng(it.latitude, it.longitude) } }
    val cameraPositionState = rememberCameraPositionState()
    var zoom by remember { mutableFloatStateOf(13f) }
    LaunchedEffect(latLngs) {
        val center = LatLngBounds.Builder()
            .takeIf { latLngs.isNotEmpty() }
            ?.apply { latLngs.forEach { include(it) } }
            ?.build()
            ?.center
            ?: LatLng(0.0, 0.0)
        cameraPositionState.position = CameraPosition.fromLatLngZoom(center, zoom)
    }
    LaunchedEffect(cameraPositionState.position) {
        zoom = cameraPositionState.position.zoom
    }
    Map(
        cameraPositionState = cameraPositionState,
    ) {
        val (drawLines, drawPoints, useSecondaryColor) =
            when (mode) {
                is LocationsViewMode.Motion -> {
                    MotionMap(latLngs, cameraPositionState)
                    Triple(false, false, false)
                }

                is LocationsViewMode.Lines -> {
                    Triple(true, true, true)
                }

                is LocationsViewMode.Points -> {
                    Triple(false, true, false)
                }
            }
        if (drawLines) {
            Polyline(points = latLngs, color = primaryMapsColor)
        }
        if (drawPoints) {
            locations.forEach {
                Circle(
                    center = LatLng(it.latitude, it.longitude),
                    clickable = true,
                    fillColor = Color.Transparent,
                    strokeColor = if (useSecondaryColor) secondaryMapsColor else primaryMapsColor,
                    radius = 0.5,
                    strokeWidth = 20f,
                    onClick = { _ ->
                        onLocationClick(it)
                    },
                )
            }
        }
    }
}

@Composable
@GoogleMapComposable
private fun MotionMap(latLngs: List<LatLng>, cameraPositionState: CameraPositionState) {
    val points = remember { mutableStateListOf<LatLng>() }
    LaunchedEffect(latLngs) {
        latLngs.forEach { latLng ->
            points.add(latLng)
            runCatching {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLng(latLng),
                    100,
                )
            }
        }
    }
    points.forEach {
        Circle(
            center = LatLng(it.latitude, it.longitude),
            clickable = true,
            fillColor = Color.Transparent,
            strokeColor = primaryMapsColor,
            radius = 0.5,
            strokeWidth = 20f,
            onClick = { },
        )
    }
}

@Composable
private fun Map(
    cameraPositionState: CameraPositionState,
    content:
    @Composable @GoogleMapComposable
    () -> Unit = {},
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        uiSettings = MapUiSettings(mapToolbarEnabled = false, zoomControlsEnabled = false),
    ) {
        content()
    }
}

@Composable
private fun OptionsPill(
    permissionsNotGranted: Boolean,
    numOfLocations: Int,
    isUploadWorkerRunning: Boolean,
    onNavToSettings: () -> Unit,
    onNavToSystemSettings: () -> Unit,
    onSingleLocation: () -> Unit,
    onUpload: () -> Unit,
    onDatePickerClick: () -> Unit,
) {
    var isPillExpanded by remember(permissionsNotGranted) {
        mutableStateOf(permissionsNotGranted)
    }
    ExpandingPill(
        isExpanded = isPillExpanded,
        onExpandOrCollapse = { isPillExpanded = !isPillExpanded },
    ) {
        if (permissionsNotGranted) {
            TintedIconButton(
                onClick = onNavToSystemSettings,
                icon = Icons.Outlined.Warning,
            )
        } else {
            if (numOfLocations > 0) {
                TintedTextButton(onClick = { }, text = numOfLocations.toString())
            }
            TintedIconButton(
                showProgress = isUploadWorkerRunning,
                onClick = onUpload,
                icon = Icons.Outlined.Upload,
            )
            TintedIconButton(
                onClick = onSingleLocation,
                icon = Icons.Outlined.AddLocation,
            )
            TintedIconButton(
                onClick = onDatePickerClick,
                icon = Icons.Outlined.Today,
            )
        }
        TintedIconButton(
            onClick = onNavToSettings,
            icon = Icons.Outlined.Settings,
        )
    }
}

@Composable
private fun ExpandingPill(
    isExpanded: Boolean,
    onExpandOrCollapse: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    var count by remember(Unit) { mutableIntStateOf(0) }
    LaunchedEffect(count) {
        if (count >= 8) {
            count = 0
        }
    }

    val buttonRotate by animateFloatAsState(
        targetValue = if (isExpanded) (90f + ((count - 1) * 90f)) else (0f + (count * 90f)),
        animationSpec = tween(100 + (count * 400), easing = LinearOutSlowInEasing),
        label = "buttonRotate",
    )
    Row(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .horizontalScroll(rememberScrollState()),
    ) {
        AnimatedVisibility(visible = isExpanded) {
            Row {
                content()
            }
        }
        TintedIconButton(
            onClick = {
                onExpandOrCollapse()
                count++
            },
            icon = Icons.Outlined.ChevronLeft,
            modifier = Modifier.rotate(buttonRotate),
        )
    }
}

@Composable
private fun TintedIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    showProgress: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Icon(
                imageVector = icon,
                null,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun TintedTextButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

private val primaryMapsColor
    @Composable
    get() = MaterialTheme.colorScheme.primary

private val secondaryMapsColor
    @Composable
    get() = MaterialTheme.colorScheme.secondary
