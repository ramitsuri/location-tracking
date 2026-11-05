package com.ramitsuri.locationtracking.ui

import android.content.Context
import com.ramitsuri.locationtracking.core.R
import com.ramitsuri.locationtracking.model.MonitoringMode

fun MonitoringMode.label(context: Context) = when (this) {
    MonitoringMode.Off -> R.string.monitoring_mode_off
    MonitoringMode.Rest -> R.string.monitoring_mode_rest
    MonitoringMode.Move -> R.string.monitoring_mode_moving
    MonitoringMode.TempRegion -> R.string.monitoring_mode_temp_region
}.let {
    context.getString(it)
}
