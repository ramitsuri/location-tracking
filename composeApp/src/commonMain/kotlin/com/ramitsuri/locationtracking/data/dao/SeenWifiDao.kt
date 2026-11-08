package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ramitsuri.locationtracking.model.SeenWifi
import com.ramitsuri.locationtracking.model.WifiSortOrder
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SeenWifiDao {
    @Transaction
    open suspend fun upsert(ssid: String) {
        val existing = get(ssid)
        if (existing == null) {
            insert(SeenWifi(ssid = ssid))
        } else {
            update(
                existing.copy(
                    seenCount = existing.seenCount + 1,
                    lastSeenAt = Clock.System.now(),
                ),
            )
        }
    }

    fun getFlow(
        query: String = "",
        sortOrder: WifiSortOrder = WifiSortOrder.MOST_TIMES,
    ): Flow<List<SeenWifi>> {
        return when (sortOrder) {
            WifiSortOrder.MOST_TIMES -> getFlowBySeenCount(query)
            WifiSortOrder.MOST_RECENT -> getFlowByLastSeen(query)
        }
    }

    @Query("SELECT * FROM seen_wifi WHERE ssid LIKE '%' || :query || '%' ORDER BY seen_count DESC, last_seen_at DESC")
    abstract fun getFlowBySeenCount(query: String = ""): Flow<List<SeenWifi>>

    @Query("SELECT * FROM seen_wifi WHERE ssid LIKE '%' || :query || '%' ORDER BY last_seen_at DESC, seen_count DESC")
    abstract fun getFlowByLastSeen(query: String = ""): Flow<List<SeenWifi>>

    @Query("SELECT * FROM seen_wifi WHERE ssid IN (:ssids) AND is_favorite = true")
    abstract suspend fun getFavorites(ssids: List<String>): List<SeenWifi>

    @Insert
    abstract suspend fun insert(wifi: SeenWifi)

    @Query("SELECT * FROM seen_wifi WHERE ssid = :ssid")
    protected abstract suspend fun get(ssid: String): SeenWifi?

    @Update
    abstract suspend fun update(wifi: SeenWifi)
}
