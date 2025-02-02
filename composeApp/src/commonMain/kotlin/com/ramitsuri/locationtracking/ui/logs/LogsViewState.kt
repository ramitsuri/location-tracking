package com.ramitsuri.locationtracking.ui.logs

import com.ramitsuri.locationtracking.model.LogItem
import kotlinx.datetime.TimeZone

data class LogsViewState(
    val timeZone: TimeZone,
    val logs: List<LogItem> = emptyList(),
    val tags: List<Tag> = emptyList(),
) {
    data class Tag(
        val value: String,
        val selected: Boolean = false,
    )
}
