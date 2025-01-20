package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ramitsuri.locationtracking.model.GeocodeCache
import java.math.BigDecimal

@Dao
interface GeocodeCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(geocodeCache: GeocodeCache)

    @Query("SELECT * FROM geocode_cache WHERE latitude = :latitude AND longitude = :longitude")
    suspend fun get(latitude: BigDecimal, longitude: BigDecimal): GeocodeCache?
}
