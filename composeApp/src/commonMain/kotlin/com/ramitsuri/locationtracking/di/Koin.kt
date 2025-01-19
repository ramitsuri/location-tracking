package com.ramitsuri.locationtracking.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.settings.DataStoreKeyValueStore
import com.ramitsuri.locationtracking.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
        val dataStore = DataStoreKeyValueStore(get<DiFactory>()::getDataStorePath)
        Settings(dataStore)
    }

    single<AppDatabase> {
        get<DiFactory>()
            .getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get(qualifier = KoinQualifier.IO_DISPATCHER))
            .build()
    }

    single<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER) {
        Dispatchers.IO
    }
}

internal object KoinQualifier {
    val IO_DISPATCHER = named("io_dispatcher")
}
