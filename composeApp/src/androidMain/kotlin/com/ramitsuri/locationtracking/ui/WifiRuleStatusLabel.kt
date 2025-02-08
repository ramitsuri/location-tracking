package com.ramitsuri.locationtracking.ui

import android.content.Context
import com.ramitsuri.locationtracking.core.R
import com.ramitsuri.locationtracking.model.WifiMonitoringModeRule

fun WifiMonitoringModeRule.Status.label(context: Context) = when (this) {
    WifiMonitoringModeRule.Status.UNKNOWN -> null
    WifiMonitoringModeRule.Status.CONNECTED -> R.string.wifi_rule_status_connected
    WifiMonitoringModeRule.Status.DISCONNECTED -> R.string.wifi_rule_status_disconnected
}?.let {
    context.getString(it)
} ?: ""
