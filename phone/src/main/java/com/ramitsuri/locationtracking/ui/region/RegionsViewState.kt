package com.ramitsuri.locationtracking.ui.region

import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Region

data class RegionsViewState(
    val regions: List<Region> = listOf(),
    val unsavedRegion: Region = Region.EMPTY,
    val initialMapCenter: LatLng = LatLng.NY,
) {
    val isUnsavedRegionEmpty = unsavedRegion == Region.EMPTY

    val unsavedRegionNeedsName = unsavedRegion.latLngs.size >= 3 && unsavedRegion.name.isBlank()

    val canSaveRegion = unsavedRegion.latLngs.size >= 3 &&
        unsavedRegion.name.isNotBlank()
}
