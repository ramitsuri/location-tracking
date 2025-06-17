package com.ramitsuri.locationtracking.tiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.services.BackgroundService
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.battery.BatteryInfoProvider
import com.ramitsuri.locationtracking.util.getIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class MonitoringModeTileService : TileService(), KoinComponent {
    private val settings: Settings by inject()
    private val coroutineScope: CoroutineScope by inject()
    private val batteryInfoProvider: BatteryInfoProvider by inject()
    private var job: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        job?.cancel()
    }

    override fun onClick() {
        super.onClick()
        coroutineScope.launch {
            settings.setMonitoringMode(settings.getMonitoringMode().first().getNextMode())
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        job?.cancel()
        job = coroutineScope.launch {
            combine(
                BackgroundService.isRunning,
                settings.getMonitoringMode(),
            ) { isServiceRunning, monitoringMode ->
                isServiceRunning to monitoringMode
            }.collect { (isServiceRunning, monitoringMode) ->
                val actualMonitoringMode = if (isServiceRunning) {
                    monitoringMode
                } else {
                    MonitoringMode.Off
                }
                tile.state =
                    if (actualMonitoringMode == MonitoringMode.Off) {
                        Tile.STATE_INACTIVE
                    } else {
                        Tile.STATE_ACTIVE
                    }
                tile.label = "${batteryInfoProvider.getLevel()}%"
                tile.icon = Icon.createWithResource(
                    this@MonitoringModeTileService,
                    actualMonitoringMode.getIcon(),
                )
                tile.updateTile()
            }
        }
    }
}
