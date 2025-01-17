package com.ramitsuri.locationtracking.di

import androidx.room.RoomDatabase
import com.ramitsuri.locationtracking.data.AppDatabase

interface DiFactory {
    fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
}
