package com.ramitsuri.locationtracking.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
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

fun LocalTime.format(): String {
    return LocalTime.Format {
        hour(padding = Padding.ZERO)
        char(':')
        minute(padding = Padding.ZERO)
    }.format(this)
}

fun Instant.format(timeZone: TimeZone, am: String, pm: String): String {
    return LocalDateTime.Format {
        monthNumber(padding = Padding.NONE)
        char('/')
        dayOfMonth(padding = Padding.NONE)
        char('/')
        year(padding = Padding.ZERO)
        char(',')
        char(' ')
        amPmHour(padding = Padding.NONE)
        char(':')
        minute(padding = Padding.ZERO)
        char(' ')
        amPmMarker(am = am, pm = pm)
    }.format(this.toLocalDateTime(timeZone))
}

fun Instant.formatForLogs(timeZone: TimeZone, amString: String, pmString: String): String {
    return LocalDateTime.Format {
        amPmHour(padding = Padding.NONE)
        char(':')
        minute()
        char(':')
        second()
        char('.')
        secondFraction(maxLength = 3)
        char(' ')
        amPmMarker(am = amString, pm = pmString)
    }.format(this.toLocalDateTime(timeZone))
}
