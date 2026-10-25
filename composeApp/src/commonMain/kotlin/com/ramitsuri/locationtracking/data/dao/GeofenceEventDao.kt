package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ramitsuri.locationtracking.model.GeofenceEvent
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: GeofenceEvent)

    @Query("SELECT * FROM geofence_events WHERE time >= :since ORDER BY time DESC")
    fun getEventsFlow(since: Instant): Flow<List<GeofenceEvent>>

    @Query("SELECT * FROM geofence_events ORDER BY time DESC LIMIT :limit")
    suspend fun getRecentEvents(limit: Int): List<GeofenceEvent>

    @Query("DELETE FROM geofence_events WHERE time < :before")
    suspend fun deleteOlderThan(before: Instant)
}
