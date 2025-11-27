package com.ramitsuri.locationtracking.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// Adds is_favorite to seen wifi table
class Migration6To7 : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE 'seen_wifi' " +
                "ADD COLUMN `is_favorite` INTEGER NOT NULL DEFAULT 0",
        )
    }
}
