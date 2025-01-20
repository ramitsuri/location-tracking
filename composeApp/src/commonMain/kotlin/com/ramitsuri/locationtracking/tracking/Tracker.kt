package com.ramitsuri.locationtracking.tracking

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.location.Request
import com.ramitsuri.locationtracking.tracking.location.forMonitoringMode
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import java.math.RoundingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Tracker(
    private val permissionChecker: PermissionChecker,
    private val locationProvider: LocationProvider,
    private val wifiInfoProvider: WifiInfoProvider,
    private val locationRepository: LocationRepository,
    private val geocoderRepository: GeocoderRepository,
    private val settings: Settings,
    private val scope: CoroutineScope,
) {

    private var locationCollectionJob: Job? = null
    private var monitoringModeCollectionJob: Job? = null
    private var reverseGeocodeJob: Job? = null

    private val _lastKnownAddressOrLocation: MutableStateFlow<String?> = MutableStateFlow(null)
    val lastKnownAddressOrLocation = _lastKnownAddressOrLocation.asStateFlow()

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
                saveAddressOf(location)
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
                                    saveAddressOf(location)
                                }
                        }
                    }
                }
        }
    }

    private fun saveAddressOf(location: Location) {
        // Put coordinates right away while we wait to get address so that we're able to show
        // something to the user indicating the location has changed
        _lastKnownAddressOrLocation.update {
            val lat = location.latitude.toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
            val lon = location.longitude.toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
            "$lat, $lon"
        }
        reverseGeocodeJob?.cancel()
        reverseGeocodeJob = scope.launch {
            delay(500)
            geocoderRepository.reverseGeocode(
                location.latitude,
                location.longitude,
            )?.let { address ->
                _lastKnownAddressOrLocation.update { address }
            }
        }
    }
}
