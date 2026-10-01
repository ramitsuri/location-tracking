package com.ramitsuri.locationtracking.wear

object Constants {
    /**
     * If making changes, update respective Manifest files as well. String values live there too.
     */
    object Route {
        const val MONITORING_MODE_PHONE = "/phone/monitoring-mode"
        const val MONITORING_MODE_WEAR = "/wear/monitoring-mode"
        const val SINGLE_LOCATION_PHONE = "/phone/single-location"
        const val QUICK_MODE_CHANGE_PHONE = "/phone/quick-mode-change"
    }

    object Key {
        const val MONITORING_MODE = "monitoring_mode"
    }
}
