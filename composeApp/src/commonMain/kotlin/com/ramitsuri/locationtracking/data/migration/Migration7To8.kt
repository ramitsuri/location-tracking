package com.ramitsuri.locationtracking.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// Recreates region table as center + radius instead of polygon. Existing regions are dropped.
class Migration7To8 : Migration(7, 8) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `region`")

        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `region` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`radius_meters` REAL NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`center_latitude` TEXT NOT NULL, " +
                "`center_longitude` TEXT NOT NULL" +
                ")",
        )
    }
}
