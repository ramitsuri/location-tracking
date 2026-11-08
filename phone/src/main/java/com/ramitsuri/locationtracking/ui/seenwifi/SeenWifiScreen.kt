package com.ramitsuri.locationtracking.ui.seenwifi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.SeenWifi
import com.ramitsuri.locationtracking.model.WifiSortOrder
import com.ramitsuri.locationtracking.utils.format
import kotlinx.datetime.TimeZone

@Composable
fun SeenWifiScreen(
    viewState: SeenWifiState,
    modifier: Modifier = Modifier,
    onNavBack: () -> Unit,
    onToggleIsFavorite: (String) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onSortOrderChange: (WifiSortOrder) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .statusBarsPadding()
            .displayCutoutPadding(),
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
            SortDropdown(
                selected = viewState.sortOrder,
                onSortOrderSelected = onSortOrderChange,
            )
        }
        OutlinedTextField(
            value = viewState.searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.search))
            },
            trailingIcon = {
                if (viewState.showClearButton) {
                    IconButton(
                        onClick = { onSearchTextChange("") },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                        )
                    }
                }
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            items(
                items = viewState.seenWifiList,
                key = { it.ssid },
            ) { item ->
                SeenWifiItem(
                    seenWifi = item,
                    onToggleIsFavorite = onToggleIsFavorite,
                )
            }
        }
    }
}

@Composable
private fun SortDropdown(
    selected: WifiSortOrder,
    onSortOrderSelected: (WifiSortOrder) -> Unit,
) {
    var show by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { show = !show }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.sort),
            )
        }
        DropdownMenu(
            expanded = show,
            onDismissRequest = { show = false },
        ) {
            WifiSortOrder.entries.forEach { sortOrder ->
                DropdownMenuItem(
                    text = { Text(sortOrder.label()) },
                    leadingIcon = if (sortOrder == selected) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else {
                        null
                    },
                    onClick = {
                        onSortOrderSelected(sortOrder)
                        show = false
                    },
                )
            }
        }
    }
}

@Composable
private fun WifiSortOrder.label(): String {
    return when (this) {
        WifiSortOrder.MOST_TIMES -> stringResource(R.string.sort_most_times)
        WifiSortOrder.MOST_RECENT -> stringResource(R.string.sort_most_recent)
    }
}

@Composable
private fun SeenWifiItem(
    seenWifi: SeenWifi,
    onToggleIsFavorite: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onToggleIsFavorite(seenWifi.ssid) }) {
            Icon(if (seenWifi.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder, null)
        }
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = seenWifi.ssid,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val seenCountText = pluralStringResource(
                R.plurals.seen_count_formatted,
                seenWifi.seenCount,
                seenWifi.seenCount,
            )
            val lastSeenText = seenWifi.lastSeenAt.format(
                timeZone = TimeZone.currentSystemDefault(),
                am = stringResource(R.string.am),
                pm = stringResource(R.string.pm),
            )
            Text(
                text = "$seenCountText \u2022 $lastSeenText",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
