package com.ramitsuri.locationtracking.wear.presentation.home

import com.ramitsuri.locationtracking.model.MonitoringMode

data class HomeViewState(
    val monitoringMode: MonitoringMode,
    val messagePosted: Boolean,
)
