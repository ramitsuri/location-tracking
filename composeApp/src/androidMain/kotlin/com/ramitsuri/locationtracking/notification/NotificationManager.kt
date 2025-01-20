package com.ramitsuri.locationtracking.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ramitsuri.locationtracking.MainActivity
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.services.BackgroundService.Companion.INTENT_ACTION_CHANGE_MONITORING
import com.ramitsuri.locationtracking.services.BackgroundService.Companion.INTENT_ACTION_SEND_LOCATION_USER

class NotificationManager(
    context: Context,
    private val permissionChecker: PermissionChecker,
) {
    private val appContext = context.applicationContext
    private val notificationManager = NotificationManagerCompat.from(context)

    fun createChannels() {
        // Importance min will show normal priority notification for foreground service. See
        // https://developer.android.com/reference/android/app/NotificationManager#IMPORTANCE_MIN
        // User has to actively configure this in the notification channel settings.
        NotificationChannel(
            NotificationConstants.CHANNEL_ONGOING_ID,
            appContext.getString(R.string.notification_channel_ongoing),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            description = appContext.getString(R.string.notification_channel_ongoing_description)
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
            setSound(null, null)
        }.run {
            notificationManager.createNotificationChannel(this)
        }

        NotificationChannel(
            NotificationConstants.CHANNEL_ERROR_ID,
            appContext.getString(R.string.notification_channel_errors),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }.run {
            notificationManager.createNotificationChannel(this)
        }
    }

    fun cancelBackgroundRestrictionNotification() {
        notificationManager.cancel(
            BACKGROUND_LOCATION_RESTRICTION_NOTIFICATION_TAG,
            0,
        )
    }

    @SuppressLint("MissingPermission") // Already checking for permission
    fun notifyBackgroundLocationRestriction(title: String, text: String) {
        if (!permissionChecker.hasPermission(Permission.NOTIFICATION)) {
            return
        }
        val notification =
            NotificationCompat.Builder(appContext, NotificationConstants.CHANNEL_ERROR_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(
                    PendingIntent.getActivity(
                        appContext,
                        0,
                        notificationTapIntent(),
                        UPDATE_CURRENT_INTENT_FLAGS,
                    ),
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .build()
        notificationManager.notify(
            BACKGROUND_LOCATION_RESTRICTION_NOTIFICATION_TAG,
            0,
            notification,
        )
    }

    @SuppressLint("MissingPermission") // Already checking for permission
    fun notify(notification: Notification) {
        if (!permissionChecker.hasPermission(Permission.NOTIFICATION)) {
            return
        }
        notificationManager.notify(
            NotificationConstants.NOTIFICATION_ONGOING_ID,
            notification,
        )
    }

    fun getOngoingNotification(
        title: String,
        publishActionLabel: String,
        changeMonitoringActionLabel: String,
        modeLabel: String,
    ): Notification {
        return NotificationCompat.Builder(
            appContext,
            NotificationConstants.CHANNEL_ONGOING_ID,
        )
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    appContext,
                    0,
                    notificationTapIntent(),
                    UPDATE_CURRENT_INTENT_FLAGS,
                ),
            )
            .setStyle(NotificationCompat.BigTextStyle())
            .addAction(
                0,
                publishActionLabel,
                PendingIntent.getService(
                    appContext,
                    0,
                    Intent().setAction(INTENT_ACTION_SEND_LOCATION_USER),
                    UPDATE_CURRENT_INTENT_FLAGS,
                ),
            )
            .addAction(
                0,
                changeMonitoringActionLabel,
                PendingIntent.getService(
                    appContext,
                    0,
                    Intent().setAction(INTENT_ACTION_CHANGE_MONITORING),
                    UPDATE_CURRENT_INTENT_FLAGS,
                ),
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setSound(null, AudioManager.STREAM_NOTIFICATION)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(title)
            .setContentText("")
            .setSubText(modeLabel)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun notificationTapIntent(): Intent {
        return Intent(appContext, MainActivity::class.java)
            .setAction("android.intent.action.MAIN")
            .addCategory("android.intent.category.LAUNCHER")
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    companion object {
        private const val UPDATE_CURRENT_INTENT_FLAGS =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        private const val BACKGROUND_LOCATION_RESTRICTION_NOTIFICATION_TAG =
            "backgroundRestrictionNotification"
    }
}
