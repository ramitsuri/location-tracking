package com.ramitsuri.locationtracking.util

import androidx.annotation.DrawableRes
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.MonitoringMode

@DrawableRes
fun MonitoringMode.getIcon(): Int {
    return when (this) {
        MonitoringMode.Move -> R.drawable.ic_move
        MonitoringMode.Rest -> R.drawable.ic_rest
        MonitoringMode.Off -> R.drawable.ic_off
        MonitoringMode.TempRegion -> R.drawable.ic_temp_region
    }
}
