package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ramitsuri.locationtracking.model.SeenWifi
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SeenWifiDao {
    @Transaction
    open suspend fun upsert(ssid: String) {
        val existing = get(ssid)
        if (existing == null) {
            insert(SeenWifi(ssid = ssid))
        } else {
            update(SeenWifi(ssid = ssid, seenCount = existing.seenCount + 1))
        }
    }

    @Query("SELECT * FROM seen_wifi ORDER BY seen_count DESC")
    abstract fun getFlow(): Flow<List<SeenWifi>>

    @Insert
    protected abstract suspend fun insert(wifi: SeenWifi)

    @Query("SELECT * FROM seen_wifi WHERE ssid = :ssid")
    protected abstract suspend fun get(ssid: String): SeenWifi?

    @Update
    protected abstract suspend fun update(wifi: SeenWifi)
}
