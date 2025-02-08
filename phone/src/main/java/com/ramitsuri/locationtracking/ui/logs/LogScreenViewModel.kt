package com.ramitsuri.locationtracking.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.data.dao.LogItemDao
import com.ramitsuri.locationtracking.model.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class LogScreenViewModel(
    private val logItemDao: LogItemDao,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val tagSelections: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private val minLevel = MutableStateFlow(LogLevel.INFO)
    private var initialSelectionForTags = true

    val viewState =
        combine(
            logItemDao.getTags(),
            tagSelections,
            minLevel,
        ) { tags, selections, minLevel ->
            if (initialSelectionForTags && selections.isEmpty()) {
                tagSelections.update { tags }
            }
            LogsViewState(
                timeZone = timeZone,
                tags = tags.map { LogsViewState.Tag(it, it in selections) },
                logs = logItemDao.getAll(selections, minLevel.getGreaterLogLevels()),
                minLevel = minLevel,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LogsViewState(timeZone = timeZone, minLevel = minLevel.value),
        )

    fun clearLogsClicked() {
        viewModelScope.launch {
            logItemDao.deleteAll()
        }
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

    fun levelClicked(level: LogLevel) {
        minLevel.update { level }
    }
}
