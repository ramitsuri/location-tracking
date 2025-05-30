package com.ramitsuri.locationtracking.repository

import com.ramitsuri.locationtracking.data.dao.GeocodeCacheDao
import com.ramitsuri.locationtracking.model.GeocodeCache
import com.ramitsuri.locationtracking.network.GeocoderApi
import com.ramitsuri.locationtracking.utils.toBd

class GeocoderRepository(
    private val geocoderApi: GeocoderApi,
    private val geocodeCacheDao: GeocodeCacheDao,
) {
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        val latitudeAsBd = latitude.toBd()
        val longitudeAsBd = longitude.toBd()
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
