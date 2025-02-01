package com.ramitsuri.locationtracking.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.log.DbLogWriter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone

class LogScreenViewModel(
    private val logWriter: DbLogWriter,
    private val timeZone: TimeZone,
) : ViewModel() {

    val viewState =
        logWriter
            .getAllLogs()
            .map { logs ->
                LogsViewState(
                    logs = logs,
                    timeZone = timeZone,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = LogsViewState(timeZone = timeZone),
            )

    fun clearLogsClicked() {
        logWriter.clear()
    }
}
