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

    @Query("SELECT * FROM logs ORDER BY time DESC")
    fun getAll(): Flow<List<LogItem>>
}
