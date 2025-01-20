package com.ramitsuri.locationtracking.network

interface GeocoderApi {
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String?
}
