package com.ramitsuri.locationtracking

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import co.touchlab.kermit.Logger
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.data.dao.WifiMonitoringModeRuleDao
import com.ramitsuri.locationtracking.di.KoinQualifier
import com.ramitsuri.locationtracking.di.initKoin
import com.ramitsuri.locationtracking.log.DbLogWriter
import com.ramitsuri.locationtracking.network.AndroidGeocoderApi
import com.ramitsuri.locationtracking.network.GeocoderApi
import com.ramitsuri.locationtracking.notification.NotificationManager
import com.ramitsuri.locationtracking.permissions.AndroidPermissionChecker
import com.ramitsuri.locationtracking.permissions.AndroidPermissionMonitor
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.permissions.PermissionMonitor
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.services.BackgroundService
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.tracking.battery.AndroidBatteryInfoProvider
import com.ramitsuri.locationtracking.tracking.battery.BatteryInfoProvider
import com.ramitsuri.locationtracking.tracking.location.AndroidLocationProvider
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.wifi.AndroidWifiInfoProvider
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import com.ramitsuri.locationtracking.ui.home.HomeViewModel
import com.ramitsuri.locationtracking.ui.logs.LogScreenViewModel
import com.ramitsuri.locationtracking.ui.settings.SettingsViewModel
import com.ramitsuri.locationtracking.ui.wifirule.WifiRulesViewModel
import com.ramitsuri.locationtracking.upload.UploadWorker
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import okio.Path
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class MainApp : Application(), KoinComponent {
    private val notificationManager: NotificationManager by inject<NotificationManager>()

    override fun onCreate() {
        super.onCreate()
        initDependencyInjection()
        notificationManager.createChannels()
        UploadWorker.enqueuePeriodic(this)
        Logger.addLogWriter(get<DbLogWriter>())
    }

    private fun initDependencyInjection() {
        initKoin {
            androidContext(this@MainApp)
            module {
                workManagerFactory()
                worker<UploadWorker> {
                    UploadWorker(
                        repository = get<LocationRepository>(),
                        context = androidContext(),
                        workerParams = get(),
                    )
                }
                single<LocationProvider> {
                    AndroidLocationProvider(this@MainApp)
                }

                single<WifiInfoProvider> {
                    AndroidWifiInfoProvider(
                        context = this@MainApp,
                        scope = get<CoroutineScope>(),
                    )
                }

                single<GeocoderApi> {
                    val io = get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER)
                    AndroidGeocoderApi(
                        context = this@MainApp,
                        ioDispatcher = io,
                    )
                }

                single<NotificationManager> {
                    NotificationManager(
                        context = this@MainApp,
                        permissionChecker = get<PermissionChecker>(),
                    )
                }

                single<PermissionMonitor> {
                    AndroidPermissionMonitor(
                        permissionChecker = get<PermissionChecker>(),
                    )
                }

                factory<PermissionChecker> {
                    AndroidPermissionChecker(this@MainApp)
                }

                factory<BatteryInfoProvider> {
                    AndroidBatteryInfoProvider(this@MainApp)
                }

                factory<HttpClientEngine> {
                    Android.create()
                }

                factory<Boolean>(qualifier = KoinQualifier.IS_DEBUG) {
                    BuildConfig.DEBUG
                }

                factory<RoomDatabase.Builder<AppDatabase>> {
                    val dbName = get<String>(qualifier = KoinQualifier.DATABASE_NAME)
                    val dbFile = this@MainApp.getDatabasePath(dbName)
                    Room
                        .databaseBuilder(
                            this@MainApp,
                            AppDatabase::class.java,
                            dbFile.absolutePath,
                        )
                }

                factory<Path> {
                    val fileName = get<String>(qualifier = KoinQualifier.DATASTORE_FILE_NAME)
                    this@MainApp.filesDir.resolve(fileName).absolutePath.toPath()
                }

                viewModel<SettingsViewModel> {
                    SettingsViewModel(
                        settings = get<Settings>(),
                        isUploadWorkerRunning = { UploadWorker.isRunning(this@MainApp) },
                        isServiceRunning = { BackgroundService.isRunning },
                    )
                }

                viewModel<HomeViewModel> {
                    HomeViewModel(
                        locationRepository = get<LocationRepository>(),
                        permissionState = {
                            get<PermissionMonitor>().permissionState
                        },
                        isUploadWorkerRunning = { UploadWorker.isRunning(this@MainApp) },
                        upload = { UploadWorker.enqueueImmediate(this@MainApp) },
                        timeZone = get<TimeZone>(),
                    )
                }

                viewModel<WifiRulesViewModel> {
                    WifiRulesViewModel(
                        wifiMonitoringModeRuleDao = get<WifiMonitoringModeRuleDao>(),
                    )
                }

                viewModel<LogScreenViewModel> {
                    LogScreenViewModel(
                        logWriter = get<DbLogWriter>(),
                        timeZone = get<TimeZone>(),
                    )
                }
            }
        }
    }
}
