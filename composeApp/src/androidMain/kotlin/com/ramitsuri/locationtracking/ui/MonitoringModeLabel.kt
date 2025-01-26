package com.ramitsuri.locationtracking.ui

import android.content.Context
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.MonitoringMode

fun MonitoringMode.label(context: Context) = when (this) {
    MonitoringMode.Off -> R.string.monitoring_mode_off
    MonitoringMode.Slow -> R.string.monitoring_mode_slow
    MonitoringMode.SignificantChanges -> R.string.monitoring_mode_significant_changes
    MonitoringMode.Moving -> R.string.monitoring_mode_moving
}.let {
    context.getString(it)
}
