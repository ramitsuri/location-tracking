package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region")
data class Region(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @Embedded(prefix = "center_")
    val center: LatLng,

    @ColumnInfo(name = "radius_meters")
    val radiusMeters: Float,
) {
    companion object {
        // Google recommends a minimum geofence radius of 100-150m for reliable triggering
        const val MIN_RADIUS_METERS = 100f
        const val MAX_RADIUS_METERS = 2000f
        const val DEFAULT_RADIUS_METERS = 150f
    }
}
