package com.ramitsuri.locationtracking.wear.tile

import android.content.Context
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.ramitsuri.locationtracking.settings.Settings
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalHorologistApi::class)
class TileService : SuspendingTileService(), KoinComponent {
    private val settings by inject<Settings>()

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        val tileState = TileState(settings.getMonitoringMode().first())
        val layoutElement = tileLayout(this, requestParams.deviceConfiguration, tileState)
        return Tile.Builder()
            .setResourcesVersion("")
            .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(layoutElement))
            .build()
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        return Resources.Builder()
            .setVersion(requestParams.version)
            .build()
    }

    companion object {
        fun update(applicationContext: Context) {
            getUpdater(applicationContext).requestUpdate(TileService::class.java)
        }
    }
}
