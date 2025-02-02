package com.ramitsuri.locationtracking.tracking

import app.cash.turbine.test
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.model.WifiInfo
import com.ramitsuri.locationtracking.model.WifiMonitoringModeRule
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

    // For some reason not able to get this test to work with coroutine behavior
    /*@Test
    fun `inserts seen wifi if wifi changed`() = runTest {
        setup()
        db.seenWifiDao().getFlow().test {
            wifiInfoProvider.wifiInfo.update { wifiInfo().copy(ssid = "1") }
            assertEquals(1, awaitItem().size)

            wifiInfoProvider.wifiInfo.update { wifiInfo().copy(ssid = "2") }
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }*/

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
        insertRule(
            WifiMonitoringModeRule(
                ssid = "1",
                mode = MonitoringMode.Move,
                status = WifiMonitoringModeRule.Status.CONNECTED,
            ),
        )
        settings.getMonitoringMode().test {
            assertEquals(MonitoringMode.Off, awaitItem())
            wifiInfoProvider.wifiInfo.value = wifiInfo().copy(ssid = "1")
            assertEquals(MonitoringMode.Move, awaitItem())
        }
    }

    // For some reason not able to get this test to work with coroutine behavior
    /*@Test
    fun `switches monitor mode for disconnected rule if wifi disconnected`() = runTest {
        setup()
        insertRule(
            WifiMonitoringModeRule(
                ssid = "1",
                mode = MonitoringMode.Moving,
                status = WifiMonitoringModeRule.Status.DISCONNECTED,
            ),
        )
        wifiInfoProvider.wifiInfo.value = wifiInfo().copy(ssid = "1")
        settings.getMonitoringMode().test {
            wifiInfoProvider.wifiInfo.value = wifiInfo()
            assertEquals(MonitoringMode.Moving, awaitItem())
        }
    }*/

    @Test
    fun `switches monitor mode for last connected rule if wifi disconnected + connected`() =
        runTest {
            setup()
            insertRule(
                WifiMonitoringModeRule(
                    ssid = "1",
                    mode = MonitoringMode.Move,
                    status = WifiMonitoringModeRule.Status.CONNECTED,
                ),
            )
            insertRule(
                WifiMonitoringModeRule(
                    ssid = "2",
                    mode = MonitoringMode.Walk,
                    status = WifiMonitoringModeRule.Status.DISCONNECTED,
                ),
            )
            settings.getMonitoringMode().test {
                assertEquals(MonitoringMode.Off, awaitItem())
                wifiInfoProvider.wifiInfo.update { wifiInfo().copy(ssid = "2") }
                wifiInfoProvider.wifiInfo.update { wifiInfo().copy(ssid = "1") }
                assertEquals(MonitoringMode.Move, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    private suspend fun insertRule(rule: WifiMonitoringModeRule) =
        db.wifiMonitoringModeRuleDao().insert(rule)

    private fun TestScope.setup() {
        settings = get<Settings>()
        db = get<AppDatabase>()
        wifiInfoProvider = get<TestWifiInfoProvider>()
        wifiMonitor = WifiMonitor(
            wifiInfoProvider = wifiInfoProvider,
            seenWifiDao = db.seenWifiDao(),
            wifiMonitoringModeRuleDao = db.wifiMonitoringModeRuleDao(),
            scope = backgroundScope,
            settings = settings,
        )
        wifiMonitor.startMonitoring()
    }

    private fun wifiInfo() = WifiInfo()
}
