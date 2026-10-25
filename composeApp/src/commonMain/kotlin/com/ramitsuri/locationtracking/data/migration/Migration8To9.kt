package com.ramitsuri.locationtracking.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

class Migration8To9 : Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `geofence_events` (" +
                "`id` TEXT NOT NULL, " +
                "`region_name` TEXT NOT NULL, " +
                "`transition` TEXT NOT NULL, " +
                "`time` TEXT NOT NULL, " +
                "`latitude` REAL NOT NULL, " +
                "`longitude` REAL NOT NULL, " +
                "PRIMARY KEY(`id`)" +
                ")",
        )
    }
}
