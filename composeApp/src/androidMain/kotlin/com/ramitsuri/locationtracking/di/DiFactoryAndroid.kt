package com.ramitsuri.locationtracking.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramitsuri.locationtracking.data.AppDatabase
import okio.Path
import okio.Path.Companion.toPath

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

    override fun getDataStorePath(): Path {
        val dataStoreFileName = "location_tracking.preferences_pb"
        return application.filesDir.resolve(dataStoreFileName).absolutePath.toPath()
    }
}
