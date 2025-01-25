package com.ramitsuri.locationtracking.testutils

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val testModule = module {
    factory<TestKeyValueStore> {
        TestKeyValueStore()
    }

    factory<Settings> {
        Settings(get<TestKeyValueStore>())
    }

    factory<AppDatabase> {
        Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<CoroutineDispatcher>())
            .build()
    }

    single<CoroutineDispatcher> {
        Dispatchers.IO
    }

    factory<LocationRepository> {
        LocationRepository(
            locationDao = get<AppDatabase>().locationDao(),
            locationApi = get<TestLocationApi>(),
            settings = get<Settings>(),
        )
    }

    factory<GeocoderRepository> {
        GeocoderRepository(
            geocoderApi = get<TestGeocoderApi>(),
            geocodeCacheDao = get<GeocodeCacheDao>(),
        )
    }

    factory<TestLocationProvider> {
        TestLocationProvider()
    }

    factory<TestWifiInfoProvider> {
        TestWifiInfoProvider()
    }

    factory<TestLocationApi> {
        TestLocationApi()
    }

    factory<TestGeocoderApi> {
        TestGeocoderApi()
    }

    factory<TestPermissionChecker> {
        TestPermissionChecker()
    }

    factory<LocationDao> {
        get<AppDatabase>().locationDao()
    }

    factory<GeocodeCacheDao> {
        get<AppDatabase>().geocodeCacheDao()
    }
}
