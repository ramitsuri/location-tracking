package com.ramitsuri.locationtracking.tracking

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.battery.BatteryInfoProvider
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.location.Request
import com.ramitsuri.locationtracking.tracking.location.forMonitoringMode
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Tracker(
    private val permissionChecker: PermissionChecker,
    private val locationProvider: LocationProvider,
    private val wifiInfoProvider: WifiInfoProvider,
    private val batteryInfoProvider: BatteryInfoProvider,
    private val locationRepository: LocationRepository,
    private val geocoderRepository: GeocoderRepository,
    private val settings: Settings,
    private val scope: CoroutineScope,
) {

    private var locationCollectionJob: Job? = null
    private var monitoringModeCollectionJob: Job? = null
    private var reverseGeocodeJob: Job? = null
    private var saveLastKnownLocationJob: Job? = null

    private val _lastKnownAddressOrLocation: MutableStateFlow<LocationAndAddress?> =
        MutableStateFlow(null)
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
                val locationDetailed = location.copy(
                    ssid = wifiInfo.ssid,
                    bssid = wifiInfo.bssid,
                    battery = batteryInfoProvider.getLevel(),
                    batteryStatus = batteryInfoProvider.getChargingStatus(),
                )
                locationRepository.insert(locationDetailed)
                onNewLocation(location)
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
                                .filter { it.accuracy <= MIN_HORIZONTAL_ACCURACY }
                                .collect { location ->
                                    val wifiInfo = wifiInfoProvider.wifiInfo.value
                                    val locationDetailed = location.copy(
                                        ssid = wifiInfo.ssid,
                                        bssid = wifiInfo.bssid,
                                        battery = batteryInfoProvider.getLevel(),
                                        batteryStatus = batteryInfoProvider.getChargingStatus(),
                                        monitoringMode = mode,
                                    )
                                    locationRepository.insert(locationDetailed)
                                    onNewLocation(location)
                                }
                        }
                    }
                }
        }
    }

    private fun onNewLocation(location: Location) {
        saveLastKnownLocationJob?.cancel()
        saveLastKnownLocationJob = scope.launch {
            delay(1.seconds)
            settings.setLastKnownLocation(location)
        }
        var shouldReverseGeocode = true
        // Put coordinates right away while we wait to get address so that we're able to show
        // something to the user indicating the location has changed
        _lastKnownAddressOrLocation.update { existing ->
            if (existing == null || !existing.isSameLocation(location)) {
                LocationAndAddress(location.latitude, location.longitude)
            } else {
                shouldReverseGeocode = false
                existing
            }
        }
        if (!shouldReverseGeocode) {
            return
        }
        reverseGeocodeJob?.cancel()
        reverseGeocodeJob = scope.launch {
            delay(500)
            geocoderRepository.reverseGeocode(
                location.latitude,
                location.longitude,
            )?.let { address ->
                _lastKnownAddressOrLocation.update {
                    LocationAndAddress(
                        location.latitude,
                        location.longitude,
                        address,
                    )
                }
            }
        }
    }

    data class LocationAndAddress(
        val lat: BigDecimal,
        val lon: BigDecimal,
        val address: String? = null,
    ) {
        constructor(lat: Double, lon: Double) : this(
            lat.asBd(),
            lon.asBd(),
        )

        constructor(lat: Double, lon: Double, address: String) : this(
            lat.asBd(),
            lon.asBd(),
            address,
        )

        fun isSameLocation(location: Location): Boolean {
            return lat.compareTo(location.latitude.asBd()) == 0 &&
                lon.compareTo(location.longitude.asBd()) == 0
        }

        fun string(): String = address ?: "$lat, $lon"
    }

    companion object {
        private const val MIN_HORIZONTAL_ACCURACY = 200f // In meters
    }
}

private fun Double.asBd() = this.toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
