package com.ramitsuri.locationtracking.utils

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.Region

interface RegionUtil {
    fun contains(region: Region, location: Location): Boolean
}
