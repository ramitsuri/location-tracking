package com.ramitsuri.locationtracking.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: Settings,
    isUploadWorkerRunning: () -> Flow<Boolean>,
    isServiceRunning: () -> Flow<Boolean>,
) : ViewModel() {
    val viewState = combine(
        settings.getBaseUrlFlow(),
        settings.getDeviceNameFlow(),
        isUploadWorkerRunning(),
        isServiceRunning(),
    ) { url, deviceName, isUploadWorkerRunning, isServiceRunning ->
        SettingsViewState(
            baseUrl = url,
            deviceName = deviceName,
            isUploadWorkerRunning = isUploadWorkerRunning,
            isServiceRunning = isServiceRunning,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsViewState(),
    )

    fun setBaseUrl(url: String) {
        viewModelScope.launch {
            settings.setBaseUrl(url)
        }
    }

    fun setDeviceName(deviceName: String) {
        viewModelScope.launch {
            settings.setDeviceName(deviceName)
        }
    }
}
