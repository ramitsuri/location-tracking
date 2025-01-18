package com.ramitsuri.locationtracking.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.model.Location
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        Location::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(dbBuilderProvider: () -> Builder<AppDatabase>): AppDatabase {
            if (instance == null) {
                instance = dbBuilderProvider()
                    .setDriver(BundledSQLiteDriver())
                    .setQueryCoroutineContext(Dispatchers.IO)
                    .build()
            }
            return instance as AppDatabase
        }
    }
}
