package com.ramitsuri.locationtracking

import android.app.Application
import com.ramitsuri.locationtracking.di.DiFactory
import com.ramitsuri.locationtracking.di.DiFactoryAndroid
import com.ramitsuri.locationtracking.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.dsl.module

class MainApp : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        initDependencyInjection()
    }

    private fun initDependencyInjection() {
        initKoin {
            androidContext(this@MainApp)
            module {
                single<DiFactory> {
                    DiFactoryAndroid(this@MainApp)
                }
            }
        }
    }
}
