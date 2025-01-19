package com.ramitsuri.locationtracking.tracking

import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.location.Request
import com.ramitsuri.locationtracking.tracking.location.forMonitoringMode
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class Tracker(
    private val permissionChecker: PermissionChecker,
    private val locationProvider: LocationProvider,
    private val wifiInfoProvider: WifiInfoProvider,
    private val locationRepository: LocationRepository,
    private val settings: Settings,
    private val scope: CoroutineScope,
) {

    private var locationCollectionJob: Job? = null
    private var monitoringModeCollectionJob: Job? = null

    fun startTracking() {
        wifiInfoProvider.requestUpdates()
        setupLocationRequest()
    }

    fun stopTracking() {
        wifiInfoProvider.unrequestUpdates()
        locationCollectionJob?.cancel()
        monitoringModeCollectionJob?.cancel()
    }

    fun trackSingle() {
        scope.launch {
            if (!permissionChecker.hasPermissions(
                    listOf(
                        Permission.FINE_LOCATION,
                        Permission.COARSE_LOCATION,
                    ),
                ).any { it.granted }
            ) {
                return@launch
            }
            locationProvider.requestSingle()?.let { location ->
                val wifiInfo = wifiInfoProvider.wifiInfo.value
                val locationWithWifi = location.copy(
                    ssid = wifiInfo.ssid,
                    bssid = wifiInfo.bssid,
                )
                locationRepository.insert(locationWithWifi)
            }
        }
    }

    private fun setupLocationRequest() {
        monitoringModeCollectionJob?.cancel()
        if (!permissionChecker.hasPermissions(
                listOf(
                    Permission.FINE_LOCATION,
                    Permission.COARSE_LOCATION,
                ),
            ).any { it.granted }
        ) {
            return
        }
        monitoringModeCollectionJob = scope.launch {
            settings
                .getMonitoringMode()
                .collect { mode ->
                    locationCollectionJob?.cancel()
                    val locationRequest = Request.forMonitoringMode(mode)
                    if (locationRequest != null) {
                        locationCollectionJob = launch {
                            locationProvider.requestUpdates(locationRequest)
                                .collect { location ->
                                    val wifiInfo = wifiInfoProvider.wifiInfo.value
                                    val locationWithWifi = location.copy(
                                        ssid = wifiInfo.ssid,
                                        bssid = wifiInfo.bssid,
                                    )
                                    locationRepository.insert(locationWithWifi)
                                }
                        }
                    }
                }
        }
    }
}
