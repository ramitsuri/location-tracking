package com.ramitsuri.locationtracking.di

import androidx.room.RoomDatabase
import com.ramitsuri.locationtracking.data.AppDatabase
import okio.Path

interface DiFactory {
    fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

    fun getDataStorePath(): Path
}
