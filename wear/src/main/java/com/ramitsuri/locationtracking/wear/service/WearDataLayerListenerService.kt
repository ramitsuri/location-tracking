package com.ramitsuri.locationtracking.wear.service

import android.annotation.SuppressLint
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.ramitsuri.locationtracking.data.toEnum
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.wear.Constants
import com.ramitsuri.locationtracking.wear.tile.TileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WearDataLayerListenerService : WearableListenerService(), KoinComponent {
    private val settings: Settings by inject()
    private val scope: CoroutineScope by inject()

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        logD(TAG) { "onDataChanged: $dataEvents" }
        val changeMonitoringModeEvents = mutableListOf<DataEvent>()
        dataEvents.forEach { event ->
            val path = event.dataItem.uri.path ?: ""
            when {
                path.startsWith(Constants.Route.MONITORING_MODE_WEAR) -> {
                    changeMonitoringModeEvents.add(event)
                }
            }
        }

        changeMonitoringModeEvents.lastOrNull()?.let { dataEvent ->
            logD(TAG) { "Have a change monitoring mode event" }
            val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
            val monitoringModeText = dataMap.getString(Constants.Key.MONITORING_MODE)
            if (monitoringModeText.isNullOrEmpty()) {
                logE(TAG) { "Empty or null monitoring mode" }
                return@let
            }
            val monitoringMode = toEnum(monitoringModeText, MonitoringMode.default())
            scope.launch {
                settings.setMonitoringMode(monitoringMode)
                TileService.update(applicationContext)
            }
        }
    }

    companion object {
        private const val TAG = "WearDataLayerListenerService"
    }
}
