package com.ramitsuri.locationtracking.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.utils.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
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
        settings.getMinAccuracyForDisplay(),
        settings.getPreviousBaseUrlsFlow(),
    ) {
            url, deviceName, isUploadWorkerRunning, isServiceRunning, minAccuracyForDisplay,
            previousBaseUrls,
        ->
        SettingsViewState(
            baseUrl = url,
            deviceName = deviceName,
            isUploadWorkerRunning = isUploadWorkerRunning,
            isServiceRunning = isServiceRunning,
            minAccuracyForDisplay = minAccuracyForDisplay,
            previousBaseUrls = previousBaseUrls,
        )
    }.onEach {
        logD(TAG) { "ViewState: $it" }
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

    fun setMinAccuracyForDisplay(minAccuracyForDisplay: Int) {
        viewModelScope.launch {
            settings.setMinAccuracyForDisplay(minAccuracyForDisplay)
        }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
