package com.ramitsuri.locationtracking.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

fun LocalDate.format(): String {
    return LocalDate.Format {
        monthNumber(padding = Padding.ZERO)
        char('/')
        dayOfMonth(padding = Padding.ZERO)
        char('/')
        year(padding = Padding.ZERO)
    }.format(this)
}

fun Instant.format(timeZone: TimeZone, am: String, pm: String): String {
    return LocalDateTime.Format {
        monthNumber(padding = Padding.ZERO)
        char('/')
        dayOfMonth(padding = Padding.ZERO)
        char('/')
        year(padding = Padding.ZERO)
        char(',')
        char(' ')
        hour(padding = Padding.ZERO)
        char(':')
        minute(padding = Padding.ZERO)
        char(' ')
        amPmMarker(am = am, pm = pm)
    }.format(this.toLocalDateTime(timeZone))
}
