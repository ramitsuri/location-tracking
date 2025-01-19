package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ramitsuri.locationtracking.model.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: Location)

    @Insert
    suspend fun insert(locations: List<Location>)

    @Delete
    suspend fun delete(location: Location)

    @Delete
    suspend fun delete(locations: List<Location>)

    @Query("SELECT * FROM location")
    suspend fun getAll(): List<Location>

    @Query("SELECT * FROM location")
    fun getAllFlow(): Flow<List<Location>>

    @Query("SELECT * FROM location LIMIT :limit")
    suspend fun get(limit: Int): List<Location>
}
