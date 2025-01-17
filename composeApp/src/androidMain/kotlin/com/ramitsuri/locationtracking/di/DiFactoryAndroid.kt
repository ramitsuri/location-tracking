package com.ramitsuri.locationtracking.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramitsuri.locationtracking.data.AppDatabase

class DiFactoryAndroid(private val application: Application) : DiFactory {

    override fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        val dbFile = application.getDatabasePath("app_database")
        return Room
            .databaseBuilder(
                application,
                AppDatabase::class.java,
                dbFile.absolutePath,
            )
    }
}
