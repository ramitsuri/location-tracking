package com.ramitsuri.locationtracking.location

import com.ramitsuri.locationtracking.model.MonitoringMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class Request(
    val fastestInterval: Duration? = null,
    val minUpdateDistanceMeters: Float? = null,
    val priority: Priority = Priority.BalancedPowerAccuracy,
    val interval: Duration,
) {
    companion object {
        fun forMonitoringMode(monitoringMode: MonitoringMode) = when (monitoringMode) {
            MonitoringMode.Quiet,
            MonitoringMode.Manual,
            -> {
                Request(
                    interval = 1.minutes,
                    priority = Priority.LowPower,
                    minUpdateDistanceMeters = 500f,
                    fastestInterval = 1.seconds,
                )
            }

            MonitoringMode.Significant -> {
                Request(
                    interval = 1.minutes,
                    priority = Priority.BalancedPowerAccuracy,
                    minUpdateDistanceMeters = 500f,
                    fastestInterval = 1.seconds,
                )
            }

            MonitoringMode.Move -> {
                Request(
                    interval = 10.seconds,
                    priority = Priority.HighAccuracy,
                    minUpdateDistanceMeters = null,
                    fastestInterval = 1.seconds,
                )
            }
        }
    }
}
