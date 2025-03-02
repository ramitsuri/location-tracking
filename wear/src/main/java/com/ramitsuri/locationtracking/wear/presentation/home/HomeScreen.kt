package com.ramitsuri.locationtracking.wear.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.AddLocation
import androidx.compose.material.icons.outlined.Motorcycle
import androidx.compose.material.icons.outlined.NightShelter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.ui.label
import com.ramitsuri.locationtracking.wear.presentation.theme.AppTheme

@Composable
fun HomeScreen(
    state: HomeViewState,
    onMonitoringModeChanged: (MonitoringMode) -> Unit,
    onMessagePostedAcknowledged: () -> Unit,
    onSingleLocation: () -> Unit,
    exit: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    LaunchedEffect(state.messagePosted) {
        if (state.messagePosted) {
            Toast.makeText(
                context,
                context.getString(R.string.posted),
                Toast.LENGTH_SHORT,
            ).show()
            onMessagePostedAcknowledged()
            view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
            exit()
        }
    }
    AppTheme {
        val listState = rememberScalingLazyListState()
        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(listState))
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState,
                )
            },
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                autoCentering = AutoCenteringParams(itemIndex = 0),
                state = listState,
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MonitoringMode.Move.let { mode ->
                            LargeButton(
                                modifier = Modifier.weight(1f),
                                onClick = { onMonitoringModeChanged(mode) },
                                icon = Icons.Outlined.Motorcycle,
                                contentDescription = mode.label(context),
                            )
                        }
                        MonitoringMode.Walk.let { mode ->
                            LargeButton(
                                modifier = Modifier.weight(1f),
                                onClick = { onMonitoringModeChanged(mode) },
                                icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
                                contentDescription = mode.label(context),
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MonitoringMode.Rest.let { mode ->
                            LargeButton(
                                modifier = Modifier.weight(1f),
                                onClick = { onMonitoringModeChanged(mode) },
                                icon = Icons.Outlined.NightShelter,
                                contentDescription = mode.label(context),
                            )
                        }
                        LargeButton(
                            modifier = Modifier.weight(1f),
                            onClick = onSingleLocation,
                            icon = Icons.Outlined.AddLocation,
                            contentDescription = context.getString(R.string.single_location),
                        )
                    }
                }
                item {
                    Text(text = state.monitoringMode.label(context))
                }
            }
        }
    }
}

@Composable
private fun LargeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
) {
    Button(
        modifier =
        modifier
            .padding(bottom = 8.dp),
        colors = ButtonDefaults.secondaryButtonColors(),
        onClick = onClick,
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
