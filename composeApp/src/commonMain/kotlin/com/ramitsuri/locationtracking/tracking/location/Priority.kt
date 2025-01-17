package com.ramitsuri.locationtracking.tracking.location

enum class Priority(val key: Int) {
    HighAccuracy(3),
    BalancedPowerAccuracy(2),
    LowPower(1),
    // Not using it for now
    // NoPower(0),
}
