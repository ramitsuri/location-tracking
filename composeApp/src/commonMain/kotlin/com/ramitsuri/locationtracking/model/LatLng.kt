package com.ramitsuri.locationtracking.model

import java.math.BigDecimal

data class LatLng(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
) {
    companion object {
        val NY = LatLng(BigDecimal("40.7128"), BigDecimal("-74.0060"))
    }
}
