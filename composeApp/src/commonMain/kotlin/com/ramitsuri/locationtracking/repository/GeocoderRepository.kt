package com.ramitsuri.locationtracking.repository

import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.model.GeocodeCache
import com.ramitsuri.locationtracking.network.GeocoderApi
import java.math.RoundingMode

class GeocoderRepository(
    private val geocoderApi: GeocoderApi,
    private val geocodeCacheDao: GeocodeCacheDao,
) {
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        val latitudeAsBd = latitude.toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
        val longitudeAsBd = longitude.toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
        return geocodeCacheDao.get(
            latitude = latitudeAsBd,
            longitude = longitudeAsBd,
        )
            ?.address
            ?: geocoderApi.reverseGeocode(
                latitude = latitude,
                longitude = longitude,
            )?.let { address ->
                geocodeCacheDao.insert(
                    GeocodeCache(
                        latitude = latitudeAsBd,
                        longitude = longitudeAsBd,
                        address = address,
                    ),
                )
                address
            }
    }
}
