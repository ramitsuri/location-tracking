package com.ramitsuri.locationtracking.tracking.location

import com.ramitsuri.locationtracking.model.MonitoringMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class Request(
    val fastestInterval: Duration? = null,
    val minUpdateDistanceMeters: Float? = null,
    val priority: Priority = Priority.BalancedPowerAccuracy,
    val interval: Duration,
) {
    companion object
}

fun Request.Companion.forMonitoringMode(monitoringMode: MonitoringMode) = when (monitoringMode) {
    MonitoringMode.Off -> null

    MonitoringMode.Rest -> {
        Request(
            interval = 1.hours,
            priority = Priority.BalancedPowerAccuracy,
            minUpdateDistanceMeters = 100f,
            fastestInterval = 20.minutes,
        )
    }

    MonitoringMode.Walk -> {
        Request(
            interval = 30.seconds,
            priority = Priority.HighAccuracy,
            fastestInterval = 10.seconds,
        )
    }

    MonitoringMode.Move -> {
        Request(
            interval = 10.seconds,
            priority = Priority.HighAccuracy,
        )
    }
}
