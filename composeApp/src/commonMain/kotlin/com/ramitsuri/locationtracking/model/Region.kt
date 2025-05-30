package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region")
data class Region(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "latLngs")
    val latLngs: List<LatLng>,
) {
    companion object {
        val EMPTY = Region(name = "", latLngs = listOf())
    }
}
