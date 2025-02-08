package com.ramitsuri.locationtracking.ui.wifirule

import com.ramitsuri.locationtracking.model.WifiMonitoringModeRule

data class WifiRulesViewState(
    val rules: List<WifiMonitoringModeRule> = emptyList(),
)
