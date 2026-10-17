package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.model.Location
import com.ramitsuri.locationtracking.model.Region
import com.ramitsuri.locationtracking.utils.RegionUtil

class TestRegionUtil(
    var isContains: Boolean = false,
) : RegionUtil {
    override fun contains(region: Region, location: Location): Boolean {
        return isContains
    }
}
