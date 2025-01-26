package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "seen_wifi",
)
data class SeenWifi(
    @PrimaryKey
    @ColumnInfo(name = "ssid")
    val ssid: String,

    @ColumnInfo(name = "last_seen_at")
    val lastSeenAt: Instant = Clock.System.now(),

    @ColumnInfo(name = "seen_count")
    val seenCount: Int = 1,
)
