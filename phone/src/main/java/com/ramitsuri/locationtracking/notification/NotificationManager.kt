package com.ramitsuri.locationtracking.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.ramitsuri.locationtracking.MainActivity
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.services.BackgroundService.Companion.INTENT_ACTION_CHANGE_MONITORING_NEXT
import com.ramitsuri.locationtracking.services.BackgroundService.Companion.INTENT_ACTION_CHANGE_MONITORING_NEXT_NEXT
import com.ramitsuri.locationtracking.services.BackgroundService.Companion.INTENT_ACTION_SEND_LOCATION_USER
import com.ramitsuri.locationtracking.utils.format
import java.util.Collections
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

private data class GeofenceNotificationItem(
    val regionName: String,
    val transitionName: String,
    val time: Instant,
    val latitude: Double,
    val longitude: Double,
)

class NotificationManager(
    context: Context,
    private val permissionChecker: PermissionChecker,
) {
    private val appContext = context.applicationContext
    private val notificationManager = NotificationManagerCompat.from(context)
    private val geofenceNotificationHistory =
        Collections.synchronizedList(mutableListOf<GeofenceNotificationItem>())

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

        NotificationChannel(
            NotificationConstants.CHANNEL_GEOFENCE_ID,
            appContext.getString(R.string.notification_channel_geofences),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            description =
                appContext.getString(R.string.notification_channel_geofences_description)
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

    @SuppressLint("MissingPermission")
    fun notifyGeofenceTrigger(
        transitionName: String,
        latitude: Double,
        longitude: Double,
        regionName: String? = null,
        time: Instant = Clock.System.now(),
    ) {
        if (!permissionChecker.hasPermission(Permission.NOTIFICATION)) {
            return
        }

        val regionLabel = regionName?.ifBlank { null } ?: "Region"
        val newItem = GeofenceNotificationItem(
            regionName = regionLabel,
            transitionName = transitionName,
            time = time,
            latitude = latitude,
            longitude = longitude,
        )

        synchronized(geofenceNotificationHistory) {
            geofenceNotificationHistory.add(0, newItem)
            if (geofenceNotificationHistory.size > 15) {
                geofenceNotificationHistory.removeAt(geofenceNotificationHistory.lastIndex)
            }
        }

        val mapUri = "geo:$latitude,$longitude?q=$latitude,$longitude".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val mapPendingIntent = PendingIntent.getActivity(
            appContext,
            SINGLE_GEOFENCE_NOTIFICATION_ID,
            mapIntent,
            UPDATE_CURRENT_INTENT_FLAGS,
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("Geofence Events")

        val timeZone = TimeZone.currentSystemDefault()
        val currentHistory = synchronized(geofenceNotificationHistory) { geofenceNotificationHistory.toList() }

        currentHistory.forEach { item ->
            val formattedTime = item.time.format(
                timeZone = timeZone,
                am = appContext.getString(R.string.am),
                pm = appContext.getString(R.string.pm),
            )
            val timeOnly = formattedTime.substringAfter(", ").ifEmpty { formattedTime }
            inboxStyle.addLine("- ${item.regionName}: ${item.transitionName} at $timeOnly")
        }

        val latestFormattedTime = newItem.time.format(
            timeZone = timeZone,
            am = appContext.getString(R.string.am),
            pm = appContext.getString(R.string.pm),
        ).substringAfter(", ")

        val notification =
            NotificationCompat.Builder(appContext, NotificationConstants.CHANNEL_GEOFENCE_ID)
                .setContentTitle("Geofence Events")
                .setContentText("- ${newItem.regionName}: ${newItem.transitionName} at $latestFormattedTime")
                .setStyle(inboxStyle)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(mapPendingIntent)
                .addAction(
                    0,
                    appContext.getString(R.string.view_on_map),
                    mapPendingIntent,
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        notificationManager.notify(
            SINGLE_GEOFENCE_NOTIFICATION_TAG,
            SINGLE_GEOFENCE_NOTIFICATION_ID,
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
        nextModeLabel: String,
        nextNextModeLabel: String,
        modeLabel: String,
        @DrawableRes icon: Int,
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
                nextModeLabel,
                PendingIntent.getService(
                    appContext,
                    0,
                    Intent().setAction(INTENT_ACTION_CHANGE_MONITORING_NEXT),
                    UPDATE_CURRENT_INTENT_FLAGS,
                ),
            )
            .addAction(
                0,
                nextNextModeLabel,
                PendingIntent.getService(
                    appContext,
                    0,
                    Intent().setAction(INTENT_ACTION_CHANGE_MONITORING_NEXT_NEXT),
                    UPDATE_CURRENT_INTENT_FLAGS,
                ),
            )
            .setSmallIcon(icon)
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
        private const val SINGLE_GEOFENCE_NOTIFICATION_TAG =
            "singleGeofenceNotification"
        private const val SINGLE_GEOFENCE_NOTIFICATION_ID = 4001
    }
}
