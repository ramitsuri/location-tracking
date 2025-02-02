package com.ramitsuri.locationtracking.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.LogItem
import com.ramitsuri.locationtracking.ui.components.fullBorder
import com.ramitsuri.locationtracking.ui.logs.LogsViewState
import com.ramitsuri.locationtracking.utils.formatForLogs
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogsScreen(
    viewState: LogsViewState,
    onNavBack: () -> Unit,
    onClearLogs: () -> Unit,
    onTagClick: (String) -> Unit,
    onSelectAllTags: () -> Unit,
    onUnselectAllTags: () -> Unit,
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
        var showStackTrace by remember { mutableStateOf(false) }

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
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = logData.message,
                )
            }
            if (logData.errorMessage != null || logData.stackTrace != null) {
                IconButton(onClick = { showStackTrace = !showStackTrace }) {
                    Icon(
                        imageVector =
                        if (showStackTrace) {
                            Icons.Default.ArrowDropUp
                        } else {
                            Icons.Default.ArrowDropDown
                        },
                        contentDescription = null,
                    )
                }
            }
        }
        AnimatedVisibility(showStackTrace) {
            Column {
                logData.errorMessage?.let {
                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        text = it,
                    )
                }
                logData.stackTrace?.let {
                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        text = it,
                    )
                }
            }
        }
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
                        ),
                    ),
                ),
                onNavBack = {},
                onClearLogs = {},
                onTagClick = {},
                onSelectAllTags = {},
                onUnselectAllTags = {},
            )
        }
    }
}
