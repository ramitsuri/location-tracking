package com.ramitsuri.locationtracking.ui.seenwifi

import com.ramitsuri.locationtracking.model.SeenWifi

data class SeenWifiState(
    val searchText: String = "",
    val seenWifiList: List<SeenWifi> = listOf(),
) {
    val showClearButton: Boolean
        get() = searchText.isNotEmpty()
}
