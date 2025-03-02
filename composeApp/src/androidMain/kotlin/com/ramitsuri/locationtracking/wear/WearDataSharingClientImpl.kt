package com.ramitsuri.locationtracking.wear

import android.app.Application
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.ramitsuri.locationtracking.log.logW
import com.ramitsuri.locationtracking.model.MonitoringMode
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.tasks.await

class WearDataSharingClientImpl(
    application: Application,
) : WearDataSharingClient {

    private val dataClient: DataClient = Wearable.getDataClient(application)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun postMonitoringMode(
        mode: MonitoringMode,
        to: WearDataSharingClient.To,
    ): Boolean {
        return try {
            val id = Uuid.random().toString()
            // Even though there's only going to be one event, somehow not providing a unique
            // id every time, makes it work only the first time.
            // Receiving client should resolve receiving multiple upload events.
            val path = when (to) {
                WearDataSharingClient.To.Phone -> "${Constants.Route.MONITORING_MODE_PHONE}/$id"
                WearDataSharingClient.To.Wear -> "${Constants.Route.MONITORING_MODE_WEAR}/$id"
            }
            val request =
                PutDataMapRequest.create(path)
                    .apply {
                        dataMap.putString(Constants.Key.MONITORING_MODE, mode.value)
                    }
                    .asPutDataRequest()
                    .setUrgent()

            dataClient.putDataItem(request).await()
            true
        } catch (exception: Exception) {
            logW(TAG) { "Failed to post monitoring mode: ${exception.message}" }
            false
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun postSingleLocation(to: WearDataSharingClient.To): Boolean {
        return try {
            val id = Uuid.random().toString()
            val path = when (to) {
                WearDataSharingClient.To.Phone -> "${Constants.Route.SINGLE_LOCATION_PHONE}/$id"
                WearDataSharingClient.To.Wear -> "${Constants.Route.SINGLE_LOCATION_WEAR}/$id"
            }
            val request =
                PutDataMapRequest.create(path)
                    .asPutDataRequest()
                    .setUrgent()

            dataClient.putDataItem(request).await()
            true
        } catch (exception: Exception) {
            logW(TAG) { "Failed to post single location: ${exception.message}" }
            false
        }
    }

    companion object {
        const val TAG = "WearDataSharingClient"
    }
}
