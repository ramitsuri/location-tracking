package com.ramitsuri.locationtracking

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramitsuri.locationtracking.data.AppDatabase
import com.ramitsuri.locationtracking.di.KoinQualifier
import com.ramitsuri.locationtracking.di.initKoin
import com.ramitsuri.locationtracking.network.AndroidGeocoderApi
import com.ramitsuri.locationtracking.network.GeocoderApi
import com.ramitsuri.locationtracking.notification.NotificationConstants
import com.ramitsuri.locationtracking.permissions.AndroidPermissionChecker
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.tracking.location.AndroidLocationProvider
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import com.ramitsuri.locationtracking.tracking.wifi.AndroidWifiInfoProvider
import com.ramitsuri.locationtracking.tracking.wifi.WifiInfoProvider
import com.ramitsuri.locationtracking.upload.UploadWorker
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineDispatcher
import okio.Path
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.dsl.module

class MainApp : Application(), KoinComponent {
    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }

    override fun onCreate() {
        super.onCreate()
        initDependencyInjection()
        createNotificationChannels()
        UploadWorker.enqueuePeriodic(this)
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
                    AndroidWifiInfoProvider(this@MainApp)
                }

                single<GeocoderApi> {
                    val io = get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER)
                    AndroidGeocoderApi(
                        context = this@MainApp,
                        ioDispatcher = io,
                    )
                }

                factory<PermissionChecker> {
                    AndroidPermissionChecker(this@MainApp)
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
            }
        }
    }

    private fun createNotificationChannels() {
        // Importance min will show normal priority notification for foreground service. See
        // https://developer.android.com/reference/android/app/NotificationManager#IMPORTANCE_MIN
        // User has to actively configure this in the notification channel settings.
        NotificationChannel(
            NotificationConstants.CHANNEL_ONGOING_ID,
            getString(R.string.notification_channel_ongoing),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            description = getString(R.string.notification_channel_ongoing_description)
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
            setSound(null, null)
        }.run {
            notificationManager.createNotificationChannel(this)
        }

        NotificationChannel(
            NotificationConstants.CHANNEL_ERROR_ID,
            getString(R.string.notification_channel_errors),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }.run {
            notificationManager.createNotificationChannel(this)
        }
    }
}
