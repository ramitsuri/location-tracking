package com.ramitsuri.locationtracking.model

import co.touchlab.kermit.Severity
import com.ramitsuri.locationtracking.data.DbEnum

enum class LogLevel(override val value: String) : DbEnum {
    DEBUG(value = "debug"),
    INFO(value = "info"),
    WARNING(value = "warning"),
    ERROR(value = "error"),
    ;

    fun getGreaterLogLevels(): List<LogLevel> {
        return when (this) {
            DEBUG -> listOf(DEBUG, INFO, WARNING, ERROR)
            INFO -> listOf(INFO, WARNING, ERROR)
            WARNING -> listOf(WARNING, ERROR)
            ERROR -> listOf(ERROR)
        }
    }
}

fun Severity.toLogLevel(): LogLevel {
    return when (this) {
        Severity.Debug -> LogLevel.DEBUG
        Severity.Info -> LogLevel.INFO
        Severity.Warn -> LogLevel.WARNING
        Severity.Error -> LogLevel.ERROR
        else -> LogLevel.INFO
    }
}
