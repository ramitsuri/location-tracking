package com.ramitsuri.locationtracking.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// Adds seen wifi table and wifi monitoring mode rules table
class Migration2To3 : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `seen_wifi` (`ssid` TEXT NOT NULL, " +
                "`last_seen_at` TEXT NOT NULL, `seen_count` INTEGER NOT NULL, " +
                "PRIMARY KEY(`ssid`))",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `wifi_monitoring_mode_rules` (`id` TEXT NOT NULL, " +
                "`ssid` TEXT NOT NULL, `status` TEXT NOT NULL, `mode` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`))",
        )
    }
}
