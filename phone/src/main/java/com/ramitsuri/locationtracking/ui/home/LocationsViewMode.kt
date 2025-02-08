package com.ramitsuri.locationtracking.ui.home

sealed interface LocationsViewMode {
    data object Points : LocationsViewMode
    data object Lines : LocationsViewMode
    data object Motion : LocationsViewMode
}
