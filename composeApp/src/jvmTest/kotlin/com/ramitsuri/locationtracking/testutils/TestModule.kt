package com.ramitsuri.locationtracking.testutils

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.network.GeocoderApi
import com.ramitsuri.locationtracking.network.LocationApi
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.KeyValueStore
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val testModule = module {
    factory<KeyValueStore> {
        TestKeyValueStore()
    }

    factory<Settings> {
        Settings(get<KeyValueStore>())
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
            locationApi = get<LocationApi>(),
            settings = get<Settings>(),
        )
    }

    factory<GeocoderRepository> {
        GeocoderRepository(
            geocoderApi = get<GeocoderApi>(),
            geocodeCacheDao = get<GeocodeCacheDao>(),
        )
    }

    factory<LocationProvider> {
        TestLocationProvider()
    }

    factory<WifiInfoProvider> {
        TestWifiInfoProvider()
    }

    factory<LocationApi> {
        TestLocationApi()
    }

    factory<GeocoderApi> {
        TestGeocoderApi()
    }

    factory<PermissionChecker> {
        TestPermissionChecker()
    }

    factory<LocationDao> {
        get<AppDatabase>().locationDao()
    }

    factory<GeocodeCacheDao> {
        get<AppDatabase>().geocodeCacheDao()
    }
}
