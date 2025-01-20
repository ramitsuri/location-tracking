package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "geocode_cache",
    indices = [
        Index(value = ["latitude", "longitude"], unique = true),
    ],
)
data class GeocodeCache
@OptIn(ExperimentalUuidApi::class)
constructor(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "latitude")
    val latitude: BigDecimal,

    @ColumnInfo(name = "longitude")
    val longitude: BigDecimal,

    @ColumnInfo(name = "address")
    val address: String,
)
