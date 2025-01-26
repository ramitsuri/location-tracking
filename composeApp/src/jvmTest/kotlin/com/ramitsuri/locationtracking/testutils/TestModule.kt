package com.ramitsuri.locationtracking.testutils

import androidx.room.Room
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.DataStoreKeyValueStore
import com.ramitsuri.locationtracking.settings.Settings
import java.nio.file.Paths
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toOkioPath
import org.koin.dsl.module

@OptIn(ExperimentalUuidApi::class)
val testModule = module {

    factory<Settings> {
        val dataStore = DataStoreKeyValueStore {
            Paths.get(BaseTest.TEMP_DIR).resolve("${Uuid.random()}.preferences_pb").toOkioPath()
        }
        Settings(dataStore)
    }

    factory<AppDatabase> {
        AppDatabase.getDb(Room.inMemoryDatabaseBuilder<AppDatabase>(), get<CoroutineDispatcher>())
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
