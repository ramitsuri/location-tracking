package com.ramitsuri.locationtracking.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.ramitsuri.locationtracking.data.dao.RegionDao
import com.ramitsuri.locationtracking.log.logE
import com.ramitsuri.locationtracking.log.logI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val regionDao: RegionDao by inject()
    private val scope: CoroutineScope by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            logE(TAG) { "Geofence event error code: ${event.errorCode}" }
            return
        }

        val transitionName = when (val transitionType = event.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "DWELL"
            else -> "UNKNOWN ($transitionType)"
        }

        val triggeringGeofences = event.triggeringGeofences ?: emptyList()
        val location = event.triggeringLocation

        val pendingResult = goAsync()
        scope.launch {
            try {
                val allRegions = regionDao.getAll()
                triggeringGeofences.forEach { geofence ->
                    val regionId = geofence.requestId.toIntOrNull()
                    val regionName = allRegions.firstOrNull { it.id == regionId }?.name
                        ?: "Region ID ${geofence.requestId}"

                    val logMessage = "Geofence $transitionName for region '$regionName' at ($location)"
                    logI(TAG) { logMessage }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
