package com.ramitsuri.locationtracking.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.AndroidLatLng
import com.ramitsuri.locationtracking.model.BatteryStatus
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LocationsViewMode
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.asAndroidPermission
import com.ramitsuri.locationtracking.ui.components.Date
import com.ramitsuri.locationtracking.ui.components.Loading
import com.ramitsuri.locationtracking.ui.components.Map
import com.ramitsuri.locationtracking.ui.components.Time
import com.ramitsuri.locationtracking.ui.components.TintedIconButton
import com.ramitsuri.locationtracking.ui.components.TintedTextButton
import com.ramitsuri.locationtracking.utils.center
import com.ramitsuri.locationtracking.utils.format
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewState: HomeViewState,
    modifier: Modifier = Modifier,
    onNavToSettings: () -> Unit,
    onNavToSystemSettings: () -> Unit,
    onSingleLocation: () -> Unit,
    onUpload: () -> Unit,
    onSelectDateTimeForLocations: (LocalDateTime, LocalDateTime?) -> Unit,
    onClearDate: () -> Unit,
    onLocationSelected: (Location) -> Unit,
    onClearSelectedLocation: () -> Unit,
    onSetLocationsViewMode: (LocationsViewMode) -> Unit,
) {
    var showDateTimePicker by remember { mutableStateOf(false) }
    val dateTimePickerState = rememberModalBottomSheetState()
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
                        missingPermissions = viewState.missingPermissions,
                        numOfLocations = viewState.numOfLocations,
                        isUploadWorkerRunning = viewState.isUploadWorkerRunning,
                        onNavToSettings = onNavToSettings,
                        onNavToSystemSettings = onNavToSystemSettings,
                        onSingleLocation = onSingleLocation,
                        onUpload = onUpload,
                        onDatePickerClick = { showDateTimePicker = true },
                    )
                },
                onClearSelectedLocation = onClearSelectedLocation,
            )
        }

        is HomeViewState.ViewMode.LocationsForDate -> {
            ScreenContent(
                modifier = modifier,
                timeZone = viewState.timeZone,
                selectedLocation = viewState.selectedLocation,
                mapContent = {
                    LocationsForDateMap(
                        mode = viewState.viewMode.mode,
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
                        TextButton(onClick = { showDateTimePicker = true }) {
                            Text(
                                text = viewState.viewMode.fromDate.format(),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        when (viewState.viewMode.mode) {
                            LocationsViewMode.Lines -> {
                                TintedIconButton(
                                    onClick = { onSetLocationsViewMode(LocationsViewMode.Motion) },
                                    icon = Icons.Outlined.SlowMotionVideo,
                                )
                            }

                            LocationsViewMode.Motion -> {
                                TintedIconButton(
                                    onClick = { onSetLocationsViewMode(LocationsViewMode.Points) },
                                    icon = Icons.Outlined.TripOrigin,
                                )
                            }

                            LocationsViewMode.Points -> {
                                TintedIconButton(
                                    onClick = { onSetLocationsViewMode(LocationsViewMode.Lines) },
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
    if (showDateTimePicker) {
        val locationsForDateViewMode = viewState.viewMode as? HomeViewState.ViewMode.LocationsForDate
        DateTimePicker(
            initialFromDate = locationsForDateViewMode?.fromDate,
            initialFromTime = locationsForDateViewMode?.fromTime ?: LocalTime(hour = 0, minute = 0),
            initialToDate = locationsForDateViewMode?.toDate,
            initialToTime = locationsForDateViewMode?.toTime ?: LocalTime(hour = 23, minute = 59),
            sheetState = dateTimePickerState,
            onDismiss = { showDateTimePicker = false },
            onDateTimeSelected = { fromDateTime, toDateTime ->
                onSelectDateTimeForLocations(fromDateTime, toDateTime)
                showDateTimePicker = false
            },
        )
    }
    if (viewState.isLoading) {
        Loading()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePicker(
    initialFromDate: LocalDate?,
    initialFromTime: LocalTime,
    initialToDate: LocalDate?,
    initialToTime: LocalTime,
    sheetState: SheetState,
    onDateTimeSelected: (LocalDateTime, LocalDateTime?) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedFromDate by remember { mutableStateOf(initialFromDate) }
    var selectedFromTime by remember { mutableStateOf(initialFromTime) }
    var selectedToDate by remember { mutableStateOf(initialToDate) }
    var selectedToTime by remember { mutableStateOf(initialToTime) }

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showFromTimePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showToTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFromDate) {
        if (selectedFromDate == null) {
            showFromDatePicker = true
        }
    }

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp),
        ) {
            DateTimeRow(
                date = selectedFromDate?.format() ?: stringResource(R.string.select_from_date),
                time = selectedFromTime.format(),
                onDateClicked = {
                    showFromDatePicker = true
                },
                onTimeClicked = {
                    showFromTimePicker = true
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            DateTimeRow(
                date = selectedToDate?.format() ?: stringResource(R.string.select_to_date),
                time = selectedToTime.format(),
                onDateClicked = {
                    showToDatePicker = true
                },
                onTimeClicked = {
                    showToTimePicker = true
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        selectedFromDate?.let { fromDate ->
                            val fromDateTime = LocalDateTime(fromDate, selectedFromTime)
                            val toDateTime = selectedToDate?.let { toDate ->
                                LocalDateTime(toDate, selectedToTime)
                            }
                            onDateTimeSelected(fromDateTime, toDateTime)
                        }
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    if (showFromDatePicker) {
        Date(
            onDismiss = { showFromDatePicker = false },
            onDateSelected = {
                selectedFromDate = it
                if (selectedToDate == null) {
                    selectedToDate = it
                }
                showFromDatePicker = false
            },
        )
    }
    if (showFromTimePicker) {
        Time(
            selectedTime = selectedFromTime,
            onTimeSelected = { selectedFromTime = it },
            onDismiss = { showFromTimePicker = false },
        )
    }
    if (showToDatePicker) {
        Date(
            onDismiss = { showToDatePicker = false },
            onDateSelected = {
                selectedToDate = it
                showToDatePicker = false
            },
        )
    }
    if (showToTimePicker) {
        Time(
            selectedTime = selectedToTime,
            onTimeSelected = { selectedToTime = it },
            onDismiss = { showToTimePicker = false },
        )
    }
}

@Composable
private fun DateTimeRow(
    date: String,
    time: String,
    onDateClicked: () -> Unit,
    onTimeClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        TextButton(onDateClicked) {
            Text(date)
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onTimeClicked) {
            Text(time)
        }
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
                AndroidLatLng(it.latitude, it.longitude)
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
    val latLngs = remember(locations) { locations.map { AndroidLatLng(it.latitude, it.longitude) } }
    val cameraPositionState = rememberCameraPositionState()
    var zoom by remember { mutableFloatStateOf(13f) }
    LaunchedEffect(latLngs) {
        val center = latLngs.center()
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
                LocationsViewMode.Motion -> {
                    MotionMap(latLngs, cameraPositionState)
                    Triple(false, false, false)
                }

                LocationsViewMode.Lines -> {
                    Triple(true, true, true)
                }

                LocationsViewMode.Points -> {
                    Triple(false, true, false)
                }
            }
        if (drawLines) {
            Polyline(points = latLngs, color = primaryMapsColor)
        }
        if (drawPoints) {
            locations.forEach {
                Circle(
                    center = AndroidLatLng(it.latitude, it.longitude),
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
private fun MotionMap(latLngs: List<AndroidLatLng>, cameraPositionState: CameraPositionState) {
    val points = remember { mutableStateListOf<AndroidLatLng>() }
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
            center = AndroidLatLng(it.latitude, it.longitude),
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
private fun OptionsPill(
    missingPermissions: List<Permission>,
    numOfLocations: Int,
    isUploadWorkerRunning: Boolean,
    onNavToSettings: () -> Unit,
    onNavToSystemSettings: () -> Unit,
    onSingleLocation: () -> Unit,
    onUpload: () -> Unit,
    onDatePickerClick: () -> Unit,
) {
    var isPillExpanded by remember(missingPermissions) {
        mutableStateOf(missingPermissions.isNotEmpty())
    }
    var showPermissionDisclosure by remember(missingPermissions) {
        mutableStateOf((missingPermissions).isNotEmpty())
    }
    ExpandingPill(
        isExpanded = isPillExpanded,
        onExpandOrCollapse = { isPillExpanded = !isPillExpanded },
    ) {
        if (missingPermissions.isNotEmpty()) {
            TintedIconButton(
                onClick = { showPermissionDisclosure = true },
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
    if (showPermissionDisclosure) {
        PermissionDisclosureDialog(
            missingPermissions = missingPermissions,
            onDismiss = { showPermissionDisclosure = false },
            onNavToSystemSettings = onNavToSystemSettings,
        )
    }
}

@Composable
private fun PermissionDisclosureDialog(
    missingPermissions: List<Permission>,
    onDismiss: () -> Unit,
    onNavToSystemSettings: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.permission_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.permission_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
                if (missingPermissions.contains(Permission.FINE_LOCATION) &&
                    missingPermissions.contains(Permission.COARSE_LOCATION)
                ) {
                    val permission = if (missingPermissions.contains(Permission.FINE_LOCATION)) {
                        Permission.FINE_LOCATION
                    } else {
                        Permission.COARSE_LOCATION
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    MissingPermissionButton(
                        permission = permission,
                        permissionText = stringResource(R.string.permission_location),
                        onNavToSystemSettings = onNavToSystemSettings,
                    )
                }
                if (missingPermissions.contains(Permission.ACCESS_BACKGROUND_LOCATION)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MissingPermissionButton(
                        permission = Permission.ACCESS_BACKGROUND_LOCATION,
                        permissionText = stringResource(R.string.permission_background_location),
                        onNavToSystemSettings = onNavToSystemSettings,
                    )
                }
                if (missingPermissions.contains(Permission.NOTIFICATION)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MissingPermissionButton(
                        permission = Permission.NOTIFICATION,
                        permissionText = stringResource(R.string.permission_notification),
                        onNavToSystemSettings = onNavToSystemSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun MissingPermissionButton(
    permission: Permission,
    permissionText: String,
    onNavToSystemSettings: () -> Unit,
) {
    val request = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                onNavToSystemSettings()
            }
        },
    )
    val androidPermission = permission.asAndroidPermission()
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (androidPermission == null) {
                return@OutlinedButton
            }
            request.launch(androidPermission)
        },
    ) {
        Text(permissionText)
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

private val primaryMapsColor
    @Composable
    get() = MaterialTheme.colorScheme.primary

private val secondaryMapsColor
    @Composable
    get() = MaterialTheme.colorScheme.secondary
