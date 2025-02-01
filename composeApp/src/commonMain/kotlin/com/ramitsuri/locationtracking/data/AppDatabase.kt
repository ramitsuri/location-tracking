package com.ramitsuri.locationtracking.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.data.dao.LogItemDao
import com.ramitsuri.locationtracking.data.dao.SeenWifiDao
import com.ramitsuri.locationtracking.data.dao.WifiMonitoringModeRuleDao
import com.ramitsuri.locationtracking.data.migration.Migration2To3
import com.ramitsuri.locationtracking.data.migration.Migration3To4
import com.ramitsuri.locationtracking.model.GeocodeCache
import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.LogItem
import com.ramitsuri.locationtracking.model.SeenWifi
import com.ramitsuri.locationtracking.model.WifiMonitoringModeRule
import kotlinx.coroutines.CoroutineDispatcher

@Database(
    entities = [
        Location::class,
        GeocodeCache::class,
        SeenWifi::class,
        WifiMonitoringModeRule::class,
        LogItem::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    abstract fun geocodeCacheDao(): GeocodeCacheDao

    abstract fun wifiMonitoringModeRuleDao(): WifiMonitoringModeRuleDao

    abstract fun seenWifiDao(): SeenWifiDao

    abstract fun logItemDao(): LogItemDao

    companion object {
        fun getDb(builder: Builder<AppDatabase>, dispatcher: CoroutineDispatcher): AppDatabase {
            return builder
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(dispatcher)
                .addMigrations(
                    Migration2To3(),
                    Migration3To4(),
                )
                .build()
        }
    }
}
