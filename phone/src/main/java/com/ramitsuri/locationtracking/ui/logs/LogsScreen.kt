package com.ramitsuri.locationtracking.ui.logs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.LogItem
import com.ramitsuri.locationtracking.model.LogLevel
import com.ramitsuri.locationtracking.ui.AppTheme
import com.ramitsuri.locationtracking.ui.components.fullBorder
import com.ramitsuri.locationtracking.utils.formatForLogs
import kotlinx.datetime.TimeZone

@Composable
fun LogsScreen(
    viewState: LogsViewState,
    onNavBack: () -> Unit,
    onClearLogs: () -> Unit,
    onTagClick: (String) -> Unit,
    onSelectAllTags: () -> Unit,
    onUnselectAllTags: () -> Unit,
    onLevelClicked: (LogLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onNavBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
            IconButton(onClick = onClearLogs) {
                Icon(Icons.Outlined.Delete, null)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .horizontalScroll(rememberScrollState()),
        ) {
            LogLevelDropdown(selected = viewState.minLevel, onLevelClicked = onLevelClicked)
            if (viewState.tags.all { it.selected }) {
                FilterChip(
                    selected = true,
                    onClick = onUnselectAllTags,
                    label = { Text(text = stringResource(R.string.unselect_all)) },
                )
            } else {
                FilterChip(
                    selected = true,
                    onClick = onSelectAllTags,
                    label = { Text(text = stringResource(R.string.select_all)) },
                )
            }
            viewState.tags.forEach {
                FilterChip(
                    selected = it.selected,
                    onClick = {
                        onTagClick(it.value)
                    },
                    label = { Text(text = it.value) },
                )
            }
        }
        LazyColumn(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(viewState.logs, key = { it.id }) { logData ->
                LogItem(logData, viewState.timeZone)
            }
        }
    }
}

@Composable
private fun LogLevelDropdown(selected: LogLevel, onLevelClicked: (LogLevel) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            },
            selected = true,
            onClick = { show = !show },
            label = { Text(text = selected.logLevelLabel()) },
        )
        DropdownMenu(
            expanded = show,
            onDismissRequest = { show = false },
        ) {
            LogLevel.entries.forEach {
                DropdownMenuItem(
                    text = { Text(it.logLevelLabel()) },
                    onClick = {
                        onLevelClicked(it)
                        show = false
                    },
                )
            }
        }
    }
}

@Composable
private fun LogItem(logData: LogItem, timeZone: TimeZone) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .fullBorder(1.dp, MaterialTheme.colorScheme.outline, 16.dp)
            .padding(16.dp),
    ) {
        val collapsedMaxLines = 2
        var isExpanded by remember { mutableStateOf(false) }
        var clickable by remember {
            mutableStateOf(
                logData.errorMessage != null ||
                    logData.stackTrace != null,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text =
                        logData.time.formatForLogs(
                            timeZone = timeZone,
                            amString = stringResource(R.string.am),
                            pmString = stringResource(R.string.pm),
                        ),
                    )
                    if (logData.tag.isNotEmpty()) {
                        Text(
                            text = "\u2022",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        Text(
                            text = logData.tag,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                val text = buildString {
                    logData.message
                    if (logData.errorMessage != null) {
                        append("\n")
                        append(logData.errorMessage)
                    }
                    if (logData.stackTrace != null) {
                        append("\n")
                        append(logData.stackTrace)
                    }
                }
                Text(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable(enabled = clickable, onClick = { isExpanded = !isExpanded }),
                    style = MaterialTheme.typography.bodyMedium,
                    text = text,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
                    onTextLayout = { textLayoutResult ->
                        if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                            clickable = true
                        }
                    },
                )
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = logData.message,
                )
            }
            if (clickable) {
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector =
                        if (isExpanded) {
                            Icons.Default.ArrowDropUp
                        } else {
                            Icons.Default.ArrowDropDown
                        },
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun LogLevel.logLevelLabel(): String {
    return when (this) {
        LogLevel.DEBUG -> stringResource(R.string.log_level_debug)
        LogLevel.INFO -> stringResource(R.string.log_level_info)
        LogLevel.WARNING -> stringResource(R.string.log_level_warning)
        LogLevel.ERROR -> stringResource(R.string.log_level_error)
    }
}

@PreviewLightDark
@Composable
private fun LogsScreenPreview() {
    AppTheme {
        Surface {
            LogsScreen(
                viewState = LogsViewState(
                    timeZone = TimeZone.currentSystemDefault(),
                    logs = listOf(
                        LogItem(
                            message = "Log message",
                            errorMessage = "",
                            stackTrace = "",
                            tag = "Tag",
                            level = LogLevel.INFO,
                        ),
                    ),
                    minLevel = LogLevel.INFO,
                ),
                onNavBack = {},
                onClearLogs = {},
                onTagClick = {},
                onSelectAllTags = {},
                onUnselectAllTags = {},
                onLevelClicked = {},
            )
        }
    }
}
