package com.ramitsuri.locationtracking.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.util.AppUpdateManager
import com.ramitsuri.locationtracking.utils.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: Settings,
    private val updateManager: AppUpdateManager,
    isUploadWorkerRunning: () -> Flow<Boolean>,
    isServiceRunning: () -> Flow<Boolean>,
) : ViewModel() {
    private val updateAvailable = MutableStateFlow<Boolean?>(false)

    val viewState = combine(
        settings.getBaseUrlFlow(),
        settings.getDeviceNameFlow(),
        isUploadWorkerRunning(),
        isServiceRunning(),
        settings.getMinAccuracyForDisplay(),
        settings.getPreviousBaseUrlsFlow(),
        updateAvailable,
    ) {
            url, deviceName, isUploadWorkerRunning, isServiceRunning, minAccuracyForDisplay,
            previousBaseUrls, updateAvailable,
        ->
        SettingsViewState(
            baseUrl = url,
            deviceName = deviceName,
            isUploadWorkerRunning = isUploadWorkerRunning,
            isServiceRunning = isServiceRunning,
            minAccuracyForDisplay = minAccuracyForDisplay,
            previousBaseUrls = previousBaseUrls,
            updateAvailable = updateAvailable,
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

    fun onUpdateButtonClick() {
        val available = updateAvailable.value
            ?: return
        // Checking/installing if null

        if (available) { // Should install
            viewModelScope.launch {
                updateAvailable.value = null
                updateManager.downloadAndInstall()
            }
        } else { // Check if available
            viewModelScope.launch {
                updateAvailable.value = null
                updateAvailable.value = updateManager.isNewerVersionAvailable()
            }
        }

    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
