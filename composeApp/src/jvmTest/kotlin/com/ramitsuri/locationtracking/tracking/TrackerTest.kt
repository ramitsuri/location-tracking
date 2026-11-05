package com.ramitsuri.locationtracking.tracking

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.model.BatteryStatus
import com.ramitsuri.locationtracking.model.LatLng
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionResult
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.testutils.BaseTest
import com.ramitsuri.locationtracking.testutils.TestBatteryInfoProvider
import com.ramitsuri.locationtracking.testutils.TestGeocoderApi
import com.ramitsuri.locationtracking.testutils.TestLocationProvider
import com.ramitsuri.locationtracking.testutils.TestPermissionChecker
import com.ramitsuri.locationtracking.testutils.TestRegionUtil
import com.ramitsuri.locationtracking.testutils.TestWifiInfoProvider
import com.ramitsuri.locationtracking.utils.toBd
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import org.junit.Test
import org.koin.test.get
import org.koin.test.inject

class TrackerTest : BaseTest() {
    private val settings by inject<Settings>()
    private val locationProvider by inject<TestLocationProvider>()
    private val locationRepository by inject<LocationRepository>()
    private val locationDao by inject<LocationDao>()
    private val regionDao by inject<RegionDao>()
    private val regionUtil by inject<TestRegionUtil>()
    private val geocoderApi by inject<TestGeocoderApi>()
    private val geocoderRepository by lazy {
        GeocoderRepository(
            geocoderApi,
            get<GeocodeCacheDao>(),
        )
    }
    private val permissionChecker by inject<TestPermissionChecker>()
    private lateinit var tracker: Tracker

    @Test
    fun `location updates - last known with different locations`() = runTest {
        setup()
        tracker.lastKnownAddressOrLocation.test {
            assertNull(awaitItem())

            geocoderApi.address = "1, -1"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = -1.0)
            }
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), null),
            )
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), "1, -1"),
            )

            geocoderApi.address = "2, -2"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 2.0, longitude = -2.0)
            }
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("2"), BigDecimal("-2"), null),
            )
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("2"), BigDecimal("-2"), "2, -2"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `location updates - last known with different immediate locations`() = runTest {
        setup()
        tracker.lastKnownAddressOrLocation.test {
            assertNull(awaitItem())

            geocoderApi.address = "1, -1"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = -1.0)
            }
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), null),
            )
            geocoderApi.address = "2, -2"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 2.0, longitude = -2.0)
            }
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("2"), BigDecimal("-2"), null),
            )
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("2"), BigDecimal("-2"), "2, -2"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `location updates - last known with same locations`() = runTest {
        setup()
        tracker.lastKnownAddressOrLocation.test {
            assertNull(awaitItem())

            geocoderApi.address = "1, -1"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = -1.0)
            }
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), null),
            )
            geocoderApi.address = "1`, -1`"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = -1.0)
            }
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), "1`, -1`"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `single location - last known with different locations`() = runTest {
        setup(startTracker = false)
        tracker.lastKnownAddressOrLocation.test(1000.seconds) {
            assertNull(awaitItem())

            geocoderApi.address = "1, -1"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = -1.0)
            }
            tracker.trackSingle()
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), null),
            )
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), "1, -1"),
            )

            geocoderApi.address = "2, -2"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 2.0, longitude = -2.0)
            }
            tracker.trackSingle()
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("2"), BigDecimal("-2"), null),
            )
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("2"), BigDecimal("-2"), "2, -2"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `single location - last known with different immediate locations`() = runTest {
        setup(startTracker = false)
        tracker.lastKnownAddressOrLocation.test {
            assertNull(awaitItem())

            geocoderApi.address = "1, -1"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = -1.0)
            }
            tracker.trackSingle()
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), null),
            )
            geocoderApi.address = "2, -2"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 2.0, longitude = -2.0)
            }
            tracker.trackSingle()
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("2"), BigDecimal("-2"), null),
            )
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("2"), BigDecimal("-2"), "2, -2"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `single location - last known with same locations`() = runTest {
        setup(startTracker = false)
        tracker.lastKnownAddressOrLocation.test {
            assertNull(awaitItem())

            geocoderApi.address = "1, -1"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = -1.0)
            }
            tracker.trackSingle()
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), null),
            )
            geocoderApi.address = "1`, -1`"
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = -1.0)
            }
            tracker.trackSingle()
            assertLocationAndAddressEquals(
                Tracker.LocationAndAddress(BigDecimal("1"), BigDecimal("-1"), "1`, -1`"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun TestScope.setup(
        grantPermissions: Boolean = true,
        startTracker: Boolean = true,
        monitoringMode: MonitoringMode = MonitoringMode.Rest,
    ) {
        tracker = Tracker(
            locationRepository = locationRepository,
            geocoderRepository = geocoderRepository,
            locationProvider = locationProvider,
            wifiInfoProvider = get<TestWifiInfoProvider>(),
            settings = settings,
            scope = backgroundScope,
            permissionChecker = permissionChecker,
            batteryInfoProvider = get<TestBatteryInfoProvider>(),
            regionDao = regionDao,
            regionUtil = regionUtil,
        )
        if (grantPermissions) {
            permissionChecker.permissionResults.addAll(
                Permission.entries.map { PermissionResult(it, true) },
            )
        }
        settings.setMonitoringMode(monitoringMode)
        if (startTracker) {
            tracker.startTracking()
        }
    }

    @Test
    fun `temp region mode - skips recording when location is inside temp region`() = runTest {
        setup(monitoringMode = MonitoringMode.TempRegion)
        val tempRegion = Region(
            name = "temp_store",
            center = LatLng(1.0.toBd(), 1.0.toBd()),
            radiusMeters = 50f,
            type = Region.Type.TEMP,
        )
        regionDao.insert(tempRegion)

        tracker.lastKnownAddressOrLocation.test {
            assertNull(awaitItem())

            // Inside temp region: should be skipped
            regionUtil.isContains = true
            locationProvider.locationsFlow.update {
                location().copy(latitude = 1.0, longitude = 1.0, accuracy = 10)
            }

            // Outside temp region: should trigger exit and onNewLocation
            regionUtil.isContains = false
            locationProvider.locationsFlow.update {
                location().copy(latitude = 2.0, longitude = 2.0, accuracy = 10)
            }

            awaitItem() // onNewLocation for (2.0, 2.0)

            val tempRegions = regionDao.getAll().filter { it.type == Region.Type.TEMP }
            assertEquals(0, tempRegions.size)
            assertEquals(MonitoringMode.Move, settings.getMonitoringMode().first())
            val recorded = locationDao.getAll()
            assertEquals(1, recorded.size)
            assertEquals(2.0, recorded.first().latitude)
            assertEquals(MonitoringMode.Move, recorded.first().monitoringMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `temp region mode - deletes temp region, switches to Move, records location when outside temp region`() = runTest {
        setup(monitoringMode = MonitoringMode.TempRegion)
        val tempRegion = Region(
            name = "temp_store",
            center = LatLng(1.0.toBd(), 1.0.toBd()),
            radiusMeters = 50f,
            type = Region.Type.TEMP,
        )
        regionDao.insert(tempRegion)
        regionUtil.isContains = false

        tracker.lastKnownAddressOrLocation.test {
            assertNull(awaitItem())

            locationProvider.locationsFlow.update {
                location().copy(latitude = 2.0, longitude = 2.0, accuracy = 10)
            }

            awaitItem() // onNewLocation

            val tempRegions = regionDao.getAll().filter { it.type == Region.Type.TEMP }
            assertEquals(0, tempRegions.size)
            assertEquals(MonitoringMode.Move, settings.getMonitoringMode().first())
            val recorded = locationDao.getAll()
            assertEquals(1, recorded.size)
            assertEquals(MonitoringMode.Move, recorded.first().monitoringMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `temp region mode - ignores exit when accuracy is poor`() = runTest {
        setup(monitoringMode = MonitoringMode.TempRegion)
        val tempRegion = Region(
            name = "temp_store",
            center = LatLng(1.0.toBd(), 1.0.toBd()),
            radiusMeters = 50f,
            type = Region.Type.TEMP,
        )
        regionDao.insert(tempRegion)
        regionUtil.isContains = false

        tracker.lastKnownAddressOrLocation.test {
            assertNull(awaitItem())

            // Inaccurate fix (> 100m) outside region: should be ignored
            locationProvider.locationsFlow.update {
                location().copy(latitude = 2.0, longitude = 2.0, accuracy = 150)
            }

            // Accurate fix (<= 100m) outside region: triggers exit
            locationProvider.locationsFlow.update {
                location().copy(latitude = 2.0, longitude = 2.0, accuracy = 10)
            }

            awaitItem() // onNewLocation

            val tempRegions = regionDao.getAll().filter { it.type == Region.Type.TEMP }
            assertEquals(0, tempRegions.size)
            assertEquals(MonitoringMode.Move, settings.getMonitoringMode().first())
            val recorded = locationDao.getAll()
            assertEquals(1, recorded.size)
            assertEquals(10, recorded.first().accuracy)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun ReceiveTurbine<Tracker.LocationAndAddress?>.assertLocationAndAddressEquals(
        expected: Tracker.LocationAndAddress,
    ) {
        val actual: Tracker.LocationAndAddress? = awaitItem()
        actual!!
        assertEquals(0, expected.lat.compareTo(actual.lat))
        assertEquals(0, expected.lon.compareTo(actual.lon))
        assertEquals(expected.address, actual.address)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun location() = Location(
        latitude = 1.0,
        longitude = 1.0,
        ssid = "ssid",
        bssid = "bssid",
        accuracy = 1,
        messageId = Uuid.random().toString(),
        createdAt = Instant.parse("2024-12-25T12:00:00Z"),
        altitude = 1,
        verticalAccuracy = 1,
        bearing = 1,
        locationTimestamp = Instant.parse("2024-12-25T12:00:00Z"),
        velocity = 1,
        trigger = "",
        battery = null,
        batteryStatus = BatteryStatus.FULL,
        monitoringMode = MonitoringMode.Rest,
        inRegions = listOf(),
        trackerId = "",
    )
}
