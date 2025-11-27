package com.ramitsuri.locationtracking.di

import androidx.room.RoomDatabase
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.data.dao.LocationDao
import com.ramitsuri.locationtracking.data.dao.LogItemDao
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.data.dao.SeenWifiDao
import com.ramitsuri.locationtracking.log.DbLogWriter
import com.ramitsuri.locationtracking.network.GeocoderApi
import com.ramitsuri.locationtracking.network.GithubApi
import com.ramitsuri.locationtracking.network.LocationApi
import com.ramitsuri.locationtracking.network.impl.GithubApiImpl
import com.ramitsuri.locationtracking.network.impl.LocationApiImpl
import com.ramitsuri.locationtracking.repository.GeocoderRepository
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.settings.DataStoreKeyValueStore
import com.ramitsuri.locationtracking.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
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
        Settings(
            keyValueStore = dataStore,
            json = get<Json>(),
        )
    }

    single<AppDatabase> {
        val ioDispatcher = get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER)
        AppDatabase.getDb(get<RoomDatabase.Builder<AppDatabase>>(), ioDispatcher)
    }

    single<CoroutineScope> {
        CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    }

    single<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER) {
        Dispatchers.IO
    }

    single<CoroutineDispatcher>(qualifier = KoinQualifier.DEFAULT_DISPATCHER) {
        Dispatchers.Default
    }

    single<HttpClient> {
        provideHttpClient(
            clientEngine = get<HttpClientEngine>(),
            json = get<Json>(),
            enableAllLogging = get<Boolean>(qualifier = KoinQualifier.IS_DEBUG),
        )
    }

    single<Json> {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    single<LocationRepository> {
        LocationRepository(
            locationDao = get<AppDatabase>().locationDao(),
            locationApi = get<LocationApi>(),
            settings = get<Settings>(),
        )
    }

    single<GeocoderRepository> {
        GeocoderRepository(
            geocoderApi = get<GeocoderApi>(),
            geocodeCacheDao = get<GeocodeCacheDao>(),
        )
    }

    single<LocationApi> {
        LocationApiImpl(
            httpClient = get<HttpClient>(),
            ioDispatcher = get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER),
        )
    }

    single<GithubApi> {
        GithubApiImpl(
            httpClient = get<HttpClient>(),
            ioDispatcher = get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER),
        )
    }

    single<TimeZone> {
        TimeZone.currentSystemDefault()
    }

    single<DbLogWriter> {
        DbLogWriter(
            logItemDao = get<LogItemDao>(),
            scope = get<CoroutineScope>(),
        )
    }

    factory<String>(qualifier = KoinQualifier.DATABASE_NAME) {
        "app_database"
    }

    factory<String>(qualifier = KoinQualifier.DATASTORE_FILE_NAME) {
        "location_tracking-2.preferences_pb"
    }

    factory<LocationDao> {
        get<AppDatabase>().locationDao()
    }

    factory<GeocodeCacheDao> {
        get<AppDatabase>().geocodeCacheDao()
    }

    factory<SeenWifiDao> {
        get<AppDatabase>().seenWifiDao()
    }

    factory<LogItemDao> {
        get<AppDatabase>().logItemDao()
    }

    factory<RegionDao> {
        get<AppDatabase>().regionDao()
    }
}

object KoinQualifier {
    val IO_DISPATCHER = named("io_dispatcher")
    val DEFAULT_DISPATCHER = named("default_dispatcher")
    val IS_DEBUG = named("is_debug")
    val DATASTORE_FILE_NAME = named("datastore_file_name")
    val DATABASE_NAME = named("database_name")
}
