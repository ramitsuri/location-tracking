package com.ramitsuri.locationtracking.wear.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.wear.WearDataSharingClient
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    settings: Settings,
    private val dataSharingClient: WearDataSharingClient,
) : ViewModel() {
    private var monitoringModeChangeJob: Job? = null
    private val monitoringModePosted = MutableStateFlow(false)

    val viewState = combine(
        settings.getMonitoringMode(),
        monitoringModePosted,
    ) { monitoringMode, posted ->
        HomeViewState(
            monitoringMode = monitoringMode,
            monitoringModePosted = posted,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5_000),
        initialValue = HomeViewState(
            monitoringMode = MonitoringMode.default(),
            monitoringModePosted = monitoringModePosted.value,
        ),
    )

    fun onMonitoringModeChanged(monitoringMode: MonitoringMode) {
        monitoringModeChangeJob?.cancel()
        monitoringModeChangeJob = viewModelScope.launch {
            delay(1.seconds)
            val posted = dataSharingClient.postMonitoringMode(
                mode = monitoringMode,
                to = WearDataSharingClient.To.Phone,
            )
            monitoringModePosted.value = posted
        }
    }

    fun onMonitoringModePostedAcknowledged() {
        monitoringModePosted.value = false
    }
}
