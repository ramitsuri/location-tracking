package com.ramitsuri.locationtracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "logs",
)
data class LogItem
@OptIn(ExperimentalUuidApi::class)
constructor(
    @PrimaryKey
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "time")
    val time: Instant = Clock.System.now(),

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "tag")
    val tag: String,

    @ColumnInfo(name = "error_message")
    val errorMessage: String?,

    @ColumnInfo(name = "stack_trace")
    val stackTrace: String?,
)
