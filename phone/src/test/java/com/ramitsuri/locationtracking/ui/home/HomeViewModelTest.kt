package com.ramitsuri.locationtracking.ui.home

import app.cash.turbine.test
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LocationsViewMode
import com.ramitsuri.locationtracking.permissions.PermissionResult
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.ui.home.HomeViewState.ViewMode.LocationsForDate
import com.ramitsuri.locationtracking.ui.home.HomeViewState.ViewMode.LocationsForDate.Event
import com.ramitsuri.locationtracking.usecase.AddTempRegionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val locationRepository: LocationRepository = mockk(relaxed = true)
    private val settings: Settings = mockk(relaxed = true)
    private val regionDao: RegionDao = mockk(relaxed = true)
    private val addTempRegionUseCase: AddTempRegionUseCase = mockk(relaxed = true)
    private val timeZone = TimeZone.UTC
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { locationRepository.getCount() } returns flowOf(0)
        every { locationRepository.getLastKnownLocation() } returns flowOf(null)
        every { settings.getMinAccuracyForDisplay() } returns flowOf(100)
        every { settings.getLocationsViewMode() } returns flowOf(LocationsViewMode.Lines)
        every { regionDao.getAllFlow() } returns flowOf(emptyList())

        viewModel = HomeViewModel(
            locationRepository = locationRepository,
            permissionState = { MutableStateFlow<List<PermissionResult>>(emptyList()) },
            isUploadWorkerRunning = { flowOf(false) },
            upload = {},
            timeZone = timeZone,
            settings = settings,
            regionDao = regionDao,
            addTempRegionUseCase = addTempRegionUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `timeline - empty locations returns empty timeline in view state`() = runTest {
        val result = getTimelineForLocations(emptyList())
        assertEquals(emptyList(), result)
    }

    @Test
    fun `timeline - single connected location returns start event in view state`() = runTest {
        val locations = listOf(
            location(ssid = "Wifi1", time = "2025-01-01T10:00:00Z")
        )
        val result = getTimelineForLocations(locations)
        assertEquals(1, result.size)
        assertEquals(Event.Type.START, result[0].type)
        assertEquals("Wifi1", result[0].wifiName)
    }

    @Test
    fun `timeline - single disconnected location returns empty timeline in view state`() = runTest {
        val locations = listOf(
            location(ssid = null, time = "2025-01-01T10:00:00Z")
        )
        val result = getTimelineForLocations(locations)
        assertEquals(0, result.size)
    }

    @Test
    fun `timeline - disconnect event created when wifi drops in view state`() = runTest {
        val locations = listOf(
            location(ssid = "Wifi1", time = "2025-01-01T10:00:00Z"),
            location(ssid = "Wifi1", time = "2025-01-01T10:05:00Z"),
            location(ssid = null, time = "2025-01-01T10:10:00Z"),
            location(ssid = null, time = "2025-01-01T10:15:00Z"),
        )
        val result = getTimelineForLocations(locations)
        assertEquals(2, result.size)
        assertEquals(Event.Type.START, result[0].type)
        assertEquals("Wifi1", result[0].wifiName)
        assertEquals(Event.Type.DISCONNECTED, result[1].type)
        assertEquals("Wifi1", result[1].wifiName)
    }

    @Test
    fun `timeline - direct wifi switch creates disconnect and connect events in view state`() = runTest {
        val locations = listOf(
            location(ssid = "Wifi1", time = "2025-01-01T10:00:00Z"),
            location(ssid = "Wifi1", time = "2025-01-01T10:05:00Z"),
            location(ssid = "Wifi2", time = "2025-01-01T10:10:00Z"),
            location(ssid = "Wifi2", time = "2025-01-01T10:15:00Z"),
        )
        val result = getTimelineForLocations(locations)
        assertEquals(3, result.size)
        assertEquals(Event.Type.START, result[0].type)
        assertEquals("Wifi1", result[0].wifiName)
        assertEquals(Event.Type.DISCONNECTED, result[1].type)
        assertEquals("Wifi1", result[1].wifiName)
        assertEquals(Event.Type.CONNECTED, result[2].type)
        assertEquals("Wifi2", result[2].wifiName)
    }

    @Test
    fun `timeline - wifi connects after initial disconnected period in view state`() = runTest {
        val locations = listOf(
            location(ssid = null, time = "2025-01-01T10:00:00Z"),
            location(ssid = null, time = "2025-01-01T10:05:00Z"),
            location(ssid = "Wifi1", time = "2025-01-01T10:10:00Z"),
            location(ssid = "Wifi1", time = "2025-01-01T10:15:00Z"),
        )
        val result = getTimelineForLocations(locations)
        assertEquals(1, result.size)
        assertEquals(Event.Type.CONNECTED, result[0].type)
        assertEquals("Wifi1", result[0].wifiName)
    }

    @Test
    fun `onAddTempGeofence - calls addTempRegionUseCase`() = runTest {
        coEvery { addTempRegionUseCase() } returns "temp_20250101_100000"

        viewModel.onAddTempGeofence()

        coVerify { addTempRegionUseCase() }
    }

    private suspend fun getTimelineForLocations(locations: List<Location>): List<Event> {
        coEvery { locationRepository.get(any(), any(), any()) } returns locations

        var result: List<Event> = emptyList()
        viewModel.viewState.test {
            awaitItem() // initial state
            viewModel.dateTimeSelectedForLocations(
                fromDateTime = LocalDateTime.parse("2025-01-01T00:00:00"),
                toDateTime = null,
            )
            var item = awaitItem()
            while (item.isLoading) {
                item = awaitItem()
            }
            val viewMode = item.viewMode
            if (viewMode is LocationsForDate) {
                result = viewMode.timeline
            }
            cancelAndIgnoreRemainingEvents()
        }
        return result
    }

    private fun location(ssid: String?, time: String, accuracy: Int = 0): Location {
        return Location(
            latitude = 0.0,
            longitude = 0.0,
            altitude = 0,
            accuracy = accuracy,
            verticalAccuracy = 0,
            bearing = 0,
            locationTimestamp = Instant.parse(time),
            velocity = 0,
            ssid = ssid,
        )
    }
}
