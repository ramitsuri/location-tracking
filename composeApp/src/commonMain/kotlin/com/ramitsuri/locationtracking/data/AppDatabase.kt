package com.ramitsuri.locationtracking.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.model.GeocodeCache
import com.ramitsuri.locationtracking.model.Location
import kotlinx.coroutines.CoroutineDispatcher

@Database(
    entities = [
        Location::class,
        GeocodeCache::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    abstract fun geocodeCacheDao(): GeocodeCacheDao

    companion object {
        fun getDb(builder: Builder<AppDatabase>, dispatcher: CoroutineDispatcher): AppDatabase {
            return builder
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(dispatcher)
                .build()
        }
    }
}
