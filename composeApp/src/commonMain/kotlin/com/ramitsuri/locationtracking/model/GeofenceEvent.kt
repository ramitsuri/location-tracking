package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "geofence_events",
)
data class GeofenceEvent
@OptIn(ExperimentalUuidApi::class)
constructor(
    @PrimaryKey
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "region_name")
    val regionName: String,

    @ColumnInfo(name = "transition")
    val transition: String,

    @ColumnInfo(name = "time")
    val time: Instant = Clock.System.now(),

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,
)
