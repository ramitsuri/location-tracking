package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ramitsuri.locationtracking.model.LogItem
import com.ramitsuri.locationtracking.model.LogLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface LogItemDao {

    @Insert
    suspend fun insert(logItems: List<LogItem>)

    @Query("DELETE FROM logs")
    suspend fun deleteAll()

    @Query("DELETE FROM logs WHERE time < :olderThan")
    suspend fun deleteAll(olderThan: Instant)

    @Query("SELECT * FROM logs WHERE tag IN (:tags) AND level IN (:levels) ORDER BY time DESC")
    suspend fun getAll(tags: List<String>, levels: List<LogLevel>): List<LogItem>

    @Query("SELECT DISTINCT tag FROM logs ORDER BY tag")
    fun getTags(): Flow<List<String>>
}
