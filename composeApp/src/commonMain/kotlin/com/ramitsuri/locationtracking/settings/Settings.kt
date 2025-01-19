package com.ramitsuri.locationtracking.settings

import com.ramitsuri.locationtracking.data.toEnum
import com.ramitsuri.locationtracking.model.MonitoringMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Settings internal constructor(private val keyValueStore: KeyValueStore) {
    fun getMonitoringMode(): Flow<MonitoringMode> {
        return keyValueStore
            .getStringFlow(Key.MONITORING_MODE, MonitoringMode.default().value)
            .map { monitoringModeValue ->
                if (monitoringModeValue == null) {
                    MonitoringMode.default()
                } else {
                    toEnum(monitoringModeValue, MonitoringMode.default())
                }
            }
    }

    suspend fun setNextMonitoringMode() {
        val monitoringMode = getMonitoringMode().first().getNextMode()
        keyValueStore.putString(Key.MONITORING_MODE, monitoringMode.value)
    }
}
