package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ramitsuri.locationtracking.model.LogItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LogItemDao {

    @Insert
    suspend fun insert(logItem: LogItem)

    @Query("DELETE FROM logs")
    suspend fun deleteAll()

    @Query("SELECT * FROM logs WHERE tag IN (:tags) ORDER BY time DESC")
    suspend fun getAll(tags: List<String>): List<LogItem>

    @Query("SELECT DISTINCT tag FROM logs ORDER BY tag")
    fun getTags(): Flow<List<String>>
}
