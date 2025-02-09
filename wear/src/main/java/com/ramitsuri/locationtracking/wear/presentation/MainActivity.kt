package com.ramitsuri.locationtracking.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.wear.WearDataSharingClient
import com.ramitsuri.locationtracking.wear.presentation.home.HomeScreen
import com.ramitsuri.locationtracking.wear.presentation.home.HomeViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    private val dataSharingClient by inject<WearDataSharingClient>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = koinViewModel<HomeViewModel>()
            val viewState by viewModel.viewState.collectAsStateWithLifecycle()
            HomeScreen(
                state = viewState,
                onMonitoringModeChanged = viewModel::onMonitoringModeChanged,
                onMonitoringModePostedAcknowledged = viewModel::onMonitoringModePostedAcknowledged,
                exit = { finish() },
            )
        }
        // From tile
        val monitoringMode = when (intent.extras?.getString(EXTRA_KEY)) {
            MODE_MOVE -> {
                MonitoringMode.Move
            }

            MODE_WALK -> {
                MonitoringMode.Walk
            }

            MODE_REST -> {
                MonitoringMode.Rest
            }

            MODE_OFF -> {
                MonitoringMode.Off
            }

            else -> {
                null
            }
        }
        monitoringMode?.let {
            lifecycleScope.launch {
                dataSharingClient.postMonitoringMode(
                    mode = it,
                    to = WearDataSharingClient.To.Phone,
                )
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_KEY = "EXTRA_KEY"
        const val MODE_MOVE = "MODE_MOVE"
        const val MODE_WALK = "MODE_WALK"
        const val MODE_REST = "MODE_REST"
        const val MODE_OFF = "MODE_OFF"
    }
}
