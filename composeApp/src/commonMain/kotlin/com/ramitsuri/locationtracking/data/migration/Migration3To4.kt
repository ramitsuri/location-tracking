package com.ramitsuri.locationtracking.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// Adds logs table
class Migration3To4 : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `logs` (`id` TEXT NOT NULL, " +
                "`time` TEXT NOT NULL, `message` TEXT NOT NULL, `tag` TEXT NOT NULL, " +
                "`error_message` TEXT, `stack_trace` TEXT, PRIMARY KEY(`id`))",
        )
    }
}
