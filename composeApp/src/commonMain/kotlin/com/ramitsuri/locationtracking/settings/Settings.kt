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

    suspend fun setMonitoringMode(mode: MonitoringMode) {
        keyValueStore.putString(Key.MONITORING_MODE, mode.value)
    }

    suspend fun getBaseUrl(): String {
        return keyValueStore.getString(Key.BASE_URL, "") ?: ""
    }

    fun getBaseUrlFlow(): Flow<String> {
        return keyValueStore.getStringFlow(Key.BASE_URL, "").map { it.orEmpty() }
    }

    suspend fun setBaseUrl(baseUrl: String) {
        keyValueStore.putString(Key.BASE_URL, baseUrl)
    }

    suspend fun getDeviceName(): String {
        return keyValueStore.getString(Key.DEVICE_NAME, "") ?: ""
    }

    fun getDeviceNameFlow(): Flow<String> {
        return keyValueStore.getStringFlow(Key.DEVICE_NAME, "").map { it.orEmpty() }
    }

    suspend fun setDeviceName(deviceName: String) {
        keyValueStore.putString(Key.DEVICE_NAME, deviceName)
    }
}
