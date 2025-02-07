package com.ramitsuri.locationtracking.ui.settings

data class SettingsViewState(
    val isUploadWorkerRunning: Boolean = false,
    val deviceName: String = "",
    val baseUrl: String = "",
    val isServiceRunning: Boolean = false,
    val minAccuracyForDisplay: Int = 0,
)
