package com.ramitsuri.locationtracking

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import com.ramitsuri.locationtracking.di.DiFactory
import com.ramitsuri.locationtracking.di.DiFactoryAndroid
import com.ramitsuri.locationtracking.di.initKoin
import com.ramitsuri.locationtracking.permissions.AndroidPermissionChecker
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.tracking.location.AndroidLocationProvider
import com.ramitsuri.locationtracking.tracking.location.LocationProvider
import org.koin.android.ext.koin.androidContext
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
    }

    private fun initDependencyInjection() {
        initKoin {
            androidContext(this@MainApp)
            module {
                single<DiFactory> {
                    DiFactoryAndroid(this@MainApp)
                }

                single<LocationProvider> {
                    AndroidLocationProvider(this@MainApp)
                }

                factory<PermissionChecker> {
                    AndroidPermissionChecker(this@MainApp)
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
