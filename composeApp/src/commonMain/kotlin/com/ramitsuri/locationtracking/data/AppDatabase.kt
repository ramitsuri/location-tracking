package com.ramitsuri.locationtracking.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.model.Location

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
}
