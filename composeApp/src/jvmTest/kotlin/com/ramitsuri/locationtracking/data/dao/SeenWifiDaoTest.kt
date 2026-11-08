package com.ramitsuri.locationtracking.data.dao

import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.model.SeenWifi
import com.ramitsuri.locationtracking.model.WifiSortOrder
import com.ramitsuri.locationtracking.testutils.BaseTest
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.koin.test.get

class SeenWifiDaoTest : BaseTest() {
    private lateinit var dao: SeenWifiDao

    @Before
    fun setUp() {
        dao = get<AppDatabase>().seenWifiDao()
    }

    @Test
    fun `getFlow sorts by most times connected`() = runTest {
        dao.insert(
            SeenWifi(
                ssid = "wifi_1",
                seenCount = 1,
                lastSeenAt = Instant.parse("2026-11-01T00:00:00Z"),
            ),
        )
        dao.insert(
            SeenWifi(
                ssid = "wifi_2",
                seenCount = 10,
                lastSeenAt = Instant.parse("2026-10-01T00:00:00Z"),
            ),
        )
        val list = dao.getFlow(sortOrder = WifiSortOrder.MOST_TIMES).first()
        assertEquals(listOf("wifi_2", "wifi_1"), list.map { it.ssid })
    }

    @Test
    fun `getFlow sorts by most recently connected`() = runTest {
        dao.insert(
            SeenWifi(
                ssid = "wifi_1",
                seenCount = 10,
                lastSeenAt = Instant.parse("2026-10-01T00:00:00Z"),
            ),
        )
        dao.insert(
            SeenWifi(
                ssid = "wifi_2",
                seenCount = 1,
                lastSeenAt = Instant.parse("2026-11-01T00:00:00Z"),
            ),
        )
        val list = dao.getFlow(sortOrder = WifiSortOrder.MOST_RECENT).first()
        assertEquals(listOf("wifi_2", "wifi_1"), list.map { it.ssid })
    }
}
