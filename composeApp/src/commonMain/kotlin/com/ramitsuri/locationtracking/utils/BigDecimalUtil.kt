package com.ramitsuri.locationtracking.utils

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.toBd(): BigDecimal = toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
