package com.ramitsuri.locationtracking.ui.region

import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.LogItem
import com.ramitsuri.locationtracking.model.Region
import kotlinx.datetime.TimeZone

data class RegionsViewState(
    val regions: List<Region> = listOf(),
    val unsavedRegion: Region = Region.EMPTY,
    val geofenceLogs: List<LogItem> = listOf(),
    val initialMapCenter: LatLng = LatLng.NY,
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    val isUnsavedRegionEmpty = unsavedRegion == Region.EMPTY

    val unsavedRegionNeedsName = unsavedRegion.latLngs.size >= 3 && unsavedRegion.name.isBlank()

    val canSaveRegion = unsavedRegion.latLngs.size >= 3 &&
        unsavedRegion.name.isNotBlank()
}
