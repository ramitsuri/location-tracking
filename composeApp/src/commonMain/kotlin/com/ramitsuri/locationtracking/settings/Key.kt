package com.ramitsuri.locationtracking.settings

enum class Key(val value: String) {
    MONITORING_MODE("monitoring_mode"),
    BASE_URL("base_url"),
    PREVIOUS_BASE_URLS("previous_base_urls"),
    DEVICE_NAME("device_name"),
    LAST_KNOWN_LOCATION("last_known_location"),
    MIN_ACCURACY_FOR_DISPLAY("min_accuracy_for_display"),
    LOCATIONS_VIEW_MODE("locations_view_mode"),
}
