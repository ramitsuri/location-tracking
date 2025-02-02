package com.ramitsuri.locationtracking.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.log.DbLogWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone

class LogScreenViewModel(
    private val logWriter: DbLogWriter,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val tagSelections: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var initialSelectionForTags = true

    val viewState =
        combine(
            logWriter.getAllTags(),
            tagSelections,
        ) { tags, selections ->
            if (initialSelectionForTags && selections.isEmpty()) {
                tagSelections.update { tags }
            }
            LogsViewState(
                timeZone = timeZone,
                tags = tags.map { LogsViewState.Tag(it, it in selections) },
                logs = logWriter.getAllLogs(selections),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LogsViewState(timeZone = timeZone),
        )

    fun clearLogsClicked() {
        logWriter.clear()
    }

    fun tagClicked(tag: String) {
        initialSelectionForTags = false
        tagSelections.update { currentSelections ->
            if (tag in currentSelections) {
                currentSelections - tag
            } else {
                currentSelections + tag
            }
        }
    }

    fun selectAllTagsClicked() {
        initialSelectionForTags = false
        tagSelections.update {
            viewState.value.tags.map { it.value }
        }
    }

    fun unselectAllTagsClicked() {
        initialSelectionForTags = false
        tagSelections.update { listOf() }
    }
}
