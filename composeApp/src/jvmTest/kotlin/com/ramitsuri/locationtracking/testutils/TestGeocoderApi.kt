package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.network.GeocoderApi

class TestGeocoderApi : GeocoderApi {
    var address: String? = null

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        return address
    }
}
