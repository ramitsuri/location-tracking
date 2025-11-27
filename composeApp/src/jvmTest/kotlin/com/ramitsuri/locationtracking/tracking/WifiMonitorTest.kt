package com.ramitsuri.locationtracking.tracking

import app.cash.turbine.test
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.model.SeenWifi
import com.ramitsuri.locationtracking.model.WifiInfo
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.testutils.BaseTest
import com.ramitsuri.locationtracking.testutils.TestWifiInfoProvider
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get

class WifiMonitorTest : BaseTest() {
    private lateinit var settings: Settings
    private lateinit var db: AppDatabase
    private lateinit var wifiMonitor: WifiMonitor
    private lateinit var wifiInfoProvider: TestWifiInfoProvider

    @Test
    fun `inserts seen wifi if connected from disconnected`() = runTest {
        setup()
        wifiInfoProvider.wifiInfo.update { wifiInfo().copy(ssid = "1") }
        assertEquals(1, db.seenWifiDao().getFlow().first().size)
    }

    @Test
    fun `updates seen wifi if wifi reconnected`() = runTest {
        setup()
        db.seenWifiDao().upsert("1")
        wifiInfoProvider.wifiInfo.update { wifiInfo().copy(ssid = "1") }
        assertEquals(1, db.seenWifiDao().getFlow().first().size)
        assertEquals(2, db.seenWifiDao().getFlow().first().first().seenCount)
    }

    @Test
    fun `does not update or insert seen wifi if wifi disconnected`() = runTest {
        setup()
        db.seenWifiDao().upsert("1")
        wifiInfoProvider.wifiInfo.update { wifiInfo() }
        assertEquals(1, db.seenWifiDao().getFlow().first().size)
        assertEquals(1, db.seenWifiDao().getFlow().first().first().seenCount)
    }

    @Test
    fun `switches monitor mode for connected rule if wifi connected`() = runTest {
        setup()
        insertFavorite(
            ssid = "1",
        )
        settings.getMonitoringMode().test {
            assertEquals(MonitoringMode.Off, awaitItem())
            wifiInfoProvider.wifiInfo.value = wifiInfo().copy(ssid = "1")
            assertEquals(MonitoringMode.Rest, awaitItem())
        }
    }

    @Test
    fun `switches monitor mode for last connected rule if wifi disconnected + connected`() =
        runTest {
            setup()
            insertFavorite(
                ssid = "1",
            )
            insertFavorite(
                ssid = "2",
            )
            settings.getMonitoringMode().test {
                assertEquals(MonitoringMode.Off, awaitItem())
                wifiInfoProvider.wifiInfo.update { wifiInfo().copy(ssid = "2") }
                wifiInfoProvider.wifiInfo.update { wifiInfo().copy(ssid = "1") }
                assertEquals(MonitoringMode.Rest, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    private suspend fun insertFavorite(ssid: String) {
        db.seenWifiDao().insert(
            SeenWifi(
                ssid = ssid,
                isFavorite = true,
            ),
        )
    }

    private fun TestScope.setup() {
        settings = get<Settings>()
        db = get<AppDatabase>()
        wifiInfoProvider = get<TestWifiInfoProvider>()
        wifiMonitor = WifiMonitor(
            wifiInfoProvider = wifiInfoProvider,
            seenWifiDao = db.seenWifiDao(),
            scope = backgroundScope,
            settings = settings,
        )
        wifiMonitor.startMonitoring()
    }

    private fun wifiInfo() = WifiInfo()
}
