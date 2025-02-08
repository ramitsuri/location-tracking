package com.ramitsuri.locationtracking.wear.tile

import android.content.Context
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService

@OptIn(ExperimentalHorologistApi::class)
class TileService : SuspendingTileService() {
    private lateinit var renderer: TileRenderer

    override fun onCreate() {
        super.onCreate()
        renderer = TileRenderer(this)
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        val tileState = TileState
        return renderer.renderTimeline(tileState, requestParams)
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        return renderer.produceRequestedResources(Unit, requestParams)
    }

    companion object {
        fun update(applicationContext: Context) {
            getUpdater(applicationContext).requestUpdate(TileService::class.java)
        }
    }
}
