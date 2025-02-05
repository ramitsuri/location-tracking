package com.ramitsuri.locationtracking.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// Adds level to logs table
class Migration4To5 : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `logs`")

        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `logs` (`id` TEXT NOT NULL, " +
                "`time` TEXT NOT NULL, " +
                "`message` TEXT NOT NULL, " +
                "`tag` TEXT NOT NULL, " +
                "`level` TEXT NOT NULL, " +
                "`error_message` TEXT, " +
                "`stack_trace` TEXT, " +
                "PRIMARY KEY(`id`))",
        )
    }
}
