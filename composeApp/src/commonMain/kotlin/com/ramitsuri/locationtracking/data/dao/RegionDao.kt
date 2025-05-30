package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ramitsuri.locationtracking.model.Region
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(region: Region)

    @Delete
    suspend fun delete(region: Region)

    @Query("SELECT * FROM region")
    fun getAllFlow(): Flow<List<Region>>
}
