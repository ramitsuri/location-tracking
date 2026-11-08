package com.ramitsuri.locationtracking.ui.seenwifi

import com.ramitsuri.locationtracking.model.SeenWifi
import com.ramitsuri.locationtracking.model.WifiSortOrder

data class SeenWifiState(
    val searchText: String = "",
    val sortOrder: WifiSortOrder = WifiSortOrder.MOST_TIMES,
    val seenWifiList: List<SeenWifi> = listOf(),
) {
    val showClearButton: Boolean
        get() = searchText.isNotEmpty()
}
