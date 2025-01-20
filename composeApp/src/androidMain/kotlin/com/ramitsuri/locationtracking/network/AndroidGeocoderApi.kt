package com.ramitsuri.locationtracking.network

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.ramitsuri.locationtracking.log.logE
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AndroidGeocoderApi(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) : GeocoderApi {
    private val geocoder = Geocoder(context, Locale.getDefault())

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        return withContext(ioDispatcher) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCoroutine { cont ->
                        geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1,
                            object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: List<Address>) {
                                    cont.resume(addresses.address())
                                }

                                override fun onError(errorMessage: String?) {
                                    logE(TAG) { "Failed to reverse geocode: $errorMessage" }
                                    cont.resume(null)
                                }
                            },
                        )
                    }
                } else {
                    suspendCoroutine { cont ->
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocation(latitude, longitude, 1)
                            .address()
                            .let { cont.resume(it) }
                    }
                }
            } catch (e: Exception) {
                logE(TAG, e) { "Failed to reverse geocode" }
                null
            }
        }
    }

    private fun List<Address>?.address(): String? = this?.firstOrNull()?.getAddressLine(0)

    companion object {
        private const val TAG = "AndroidGeocoderApi"
    }
}
