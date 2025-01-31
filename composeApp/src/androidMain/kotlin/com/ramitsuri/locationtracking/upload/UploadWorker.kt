package com.ramitsuri.locationtracking.upload

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.locationtracking.BuildConfig
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.log.logI
import com.ramitsuri.locationtracking.notification.NotificationConstants
import com.ramitsuri.locationtracking.repository.LocationRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UploadWorker(
    private val repository: LocationRepository,
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        logI(TAG) { "doing work" }
        if (BuildConfig.DEBUG) {
            logD(TAG) { "Skipping upload in debug mode" }
            return Result.success()
        }
        val result = repository.upload()
        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification =
            NotificationCompat.Builder(
                applicationContext,
                NotificationConstants.CHANNEL_WORK_ID,
            ).apply {
                setSmallIcon(R.drawable.ic_notification)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setContentTitle(
                    applicationContext.getString(R.string.notification_uploading_locations),
                )
            }.build()
        return ForegroundInfo(NotificationConstants.NOTIFICATION_WORK_ID, notification)
    }

    companion object {
        private const val TAG = "UploadWorker"
        private const val WORK_NAME_PERIODIC = "UploadWorker"
        private const val WORK_TAG = "TAG-UploadWorker"
        private const val REPEAT_HOURS: Long = 8

        fun enqueuePeriodic(context: Context) {
            PeriodicWorkRequest
                .Builder(UploadWorker::class.java, REPEAT_HOURS, TimeUnit.HOURS)
                .addTag(WORK_TAG)
                .addTag(WORK_NAME_PERIODIC)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresCharging(true)
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build(),
                )
                .build()
                .let { request ->
                    WorkManager
                        .getInstance(context)
                        .enqueueUniquePeriodicWork(
                            WORK_NAME_PERIODIC,
                            ExistingPeriodicWorkPolicy.UPDATE,
                            request,
                        )
                }
        }

        fun enqueueImmediate(context: Context) {
            return OneTimeWorkRequest
                .Builder(UploadWorker::class)
                .addTag(WORK_TAG)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()
                .let { request ->
                    WorkManager
                        .getInstance(context)
                        .enqueue(request)
                }
        }

        fun isRunning(context: Context): Flow<Boolean> {
            return WorkManager.getInstance(context)
                .getWorkInfosByTagFlow(WORK_TAG)
                .map {
                    it.any { workInfo ->
                        workInfo.state == WorkInfo.State.RUNNING
                    }
                }
        }
    }
}
