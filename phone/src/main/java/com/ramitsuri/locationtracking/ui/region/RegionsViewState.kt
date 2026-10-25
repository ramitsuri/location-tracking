package com.ramitsuri.locationtracking.ui.region

import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LogItem
import com.ramitsuri.locationtracking.model.Region
import kotlinx.datetime.TimeZone

data class RegionsViewState(
    val regions: List<Region> = listOf(),
    val unsavedRegion: UnsavedRegion? = null,
    val geofenceLogs: List<LogItem> = listOf(),
    val lastKnownLocation: Location? = null,
    val initialMapCenter: LatLng = LatLng.NY,
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    val isUnsavedRegionEmpty = unsavedRegion == null

    val unsavedRegionNeedsName = unsavedRegion != null && unsavedRegion.name.isBlank()

    val canSaveRegion = unsavedRegion != null && unsavedRegion.name.isNotBlank()
}

data class UnsavedRegion(
    val center: LatLng,
    val radiusMeters: Float = Region.DEFAULT_RADIUS_METERS,
    val name: String = "",
)
