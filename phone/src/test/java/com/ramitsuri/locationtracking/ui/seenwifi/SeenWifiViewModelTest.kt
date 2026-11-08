package com.ramitsuri.locationtracking.ui.seenwifi

import app.cash.turbine.test
import com.ramitsuri.locationtracking.data.dao.SeenWifiDao
import com.ramitsuri.locationtracking.model.SeenWifi
import com.ramitsuri.locationtracking.model.WifiSortOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SeenWifiViewModelTest {
    private val seenWifiDao: SeenWifiDao = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSortOrderChange() = runTest {
        val wifi1 = SeenWifi(ssid = "wifi1", seenCount = 10)
        val wifi2 = SeenWifi(ssid = "wifi2", seenCount = 2)

        every { seenWifiDao.getFlow("", WifiSortOrder.MOST_TIMES) } returns flowOf(listOf(wifi1, wifi2))
        every { seenWifiDao.getFlow("", WifiSortOrder.MOST_RECENT) } returns flowOf(listOf(wifi2, wifi1))

        val viewModel = SeenWifiViewModel(seenWifiDao)

        viewModel.viewState.test {
            val initial = awaitItem()
            assertEquals(WifiSortOrder.MOST_TIMES, initial.sortOrder)
            assertEquals(listOf("wifi1", "wifi2"), initial.seenWifiList.map { it.ssid })

            viewModel.onSortOrderChange(WifiSortOrder.MOST_RECENT)
            val updated = awaitItem()
            assertEquals(WifiSortOrder.MOST_RECENT, updated.sortOrder)
            assertEquals(listOf("wifi2", "wifi1"), updated.seenWifiList.map { it.ssid })
        }
    }
}
