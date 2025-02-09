package com.ramitsuri.locationtracking.wear

import android.app.Application
import com.ramitsuri.locationtracking.di.KoinQualifier
import com.ramitsuri.locationtracking.di.initKoin
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.wear.presentation.home.HomeViewModel
import okio.Path
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
            module {
                factory<Path> {
                    val fileName = get<String>(qualifier = KoinQualifier.DATASTORE_FILE_NAME)
                    this@MainApplication.filesDir.resolve(fileName).absolutePath.toPath()
                }

                single<WearDataSharingClient> {
                    WearDataSharingClientImpl(this@MainApplication)
                }

                viewModel<HomeViewModel> {
                    HomeViewModel(
                        dataSharingClient = get<WearDataSharingClient>(),
                        settings = get<Settings>(),
                    )
                }
            }
        }
    }
}
