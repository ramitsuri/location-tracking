package com.ramitsuri.locationtracking.ui.logs

import com.ramitsuri.locationtracking.model.LogItem
import com.ramitsuri.locationtracking.model.LogLevel
import kotlinx.datetime.TimeZone

data class LogsViewState(
    val timeZone: TimeZone,
    val logs: List<LogItem> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val minLevel: LogLevel,
) {
    data class Tag(
        val value: String,
        val selected: Boolean = false,
    )
}
