package com.ramitsuri.locationtracking.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Home : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data object WifiRules : Destination

    @Serializable
    data object Logs : Destination

    @Serializable
    data object Regions : Destination
}
