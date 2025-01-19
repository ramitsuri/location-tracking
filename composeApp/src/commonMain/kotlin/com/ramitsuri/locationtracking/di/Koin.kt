package com.ramitsuri.locationtracking.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.network.LocationApi
import com.ramitsuri.locationtracking.network.impl.LocationApiImpl
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.DataStoreKeyValueStore
import com.ramitsuri.locationtracking.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.Path
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun initKoin(appModule: KoinApplication.() -> Module): KoinApplication {
    val koinApplication =
        startKoin {
            modules(
                appModule(),
                coreModule,
            )
        }
    return koinApplication
}

private val coreModule = module {
    single<Settings> {
        val dataStore = DataStoreKeyValueStore { get<Path>() }
        Settings(dataStore)
    }

    single<AppDatabase> {
        get<RoomDatabase.Builder<AppDatabase>>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(
                get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER),
            )
            .build()
    }

    single<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER) {
        Dispatchers.IO
    }

    single<HttpClient> {
        provideHttpClient(
            clientEngine = get<HttpClientEngine>(),
            enableAllLogging = get<Boolean>(qualifier = KoinQualifier.IS_DEBUG),
        )
    }

    single<LocationRepository> {
        LocationRepository(
            locationDao = get<AppDatabase>().locationDao(),
            locationApi = get<LocationApi>(),
            settings = get<Settings>(),
        )
    }

    single<LocationApi> {
        LocationApiImpl(
            httpClient = get<HttpClient>(),
            ioDispatcher = get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER),
        )
    }

    factory<String>(qualifier = KoinQualifier.DATABASE_NAME) {
        "app_database"
    }

    factory<String>(qualifier = KoinQualifier.DATASTORE_FILE_NAME) {
        "location_tracking.preferences_pb"
    }
}

internal object KoinQualifier {
    val IO_DISPATCHER = named("io_dispatcher")
    val IS_DEBUG = named("is_debug")
    val DATASTORE_FILE_NAME = named("datastore_file_name")
    val DATABASE_NAME = named("database_name")
}
