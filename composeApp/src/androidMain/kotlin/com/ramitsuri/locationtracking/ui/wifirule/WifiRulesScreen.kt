package com.ramitsuri.locationtracking.ui.wifirule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.model.WifiMonitoringModeRule
import com.ramitsuri.locationtracking.ui.label

@Composable
fun WifiRulesScreen(
    viewState: WifiRulesViewState,
    modifier: Modifier = Modifier,
    onNavBack: () -> Unit,
    onDelete: (WifiMonitoringModeRule) -> Unit,
    onEdit: (WifiMonitoringModeRule) -> Unit,
    onAdd: (WifiMonitoringModeRule) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showAddDialog = true }) {
            Text(stringResource(R.string.add))
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(
                viewState.rules,
                key = { _, item -> item.id },
            ) { index, item ->
                WifiRuleItem(
                    rule = item,
                    onDelete = { onDelete(item) },
                    onEdit = onEdit,
                )
                if (index < viewState.rules.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        modifier = Modifier
                            .height(1.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
        if (showAddDialog) {
            WifiRuleAddDialog(
                onDismiss = { showAddDialog = false },
                onAdd = onAdd,
            )
        }
    }
}

@Composable
private fun WifiRuleItem(
    rule: WifiMonitoringModeRule,
    onDelete: () -> Unit,
    onEdit: (WifiMonitoringModeRule) -> Unit,
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { showEditDialog = true }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            when (rule.status) {
                WifiMonitoringModeRule.Status.CONNECTED -> Text(
                    text = stringResource(R.string.wifi_rule_when_wifi_connected),
                )

                WifiMonitoringModeRule.Status.DISCONNECTED -> Text(
                    text = stringResource(R.string.wifi_rule_when_wifi_disconnected),
                )

                WifiMonitoringModeRule.Status.UNKNOWN -> Unit
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rule.ssid,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.wifi_rule_change_mode),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rule.mode.label(context),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, null)
        }
    }
    if (showEditDialog) {
        WifiRuleEditDialog(
            rule = rule,
            onDismiss = { showEditDialog = false },
            onEdit = onEdit,
        )
    }
}

@Composable
private fun WifiRuleEditDialog(
    rule: WifiMonitoringModeRule,
    onDismiss: () -> Unit,
    onEdit: (WifiMonitoringModeRule) -> Unit,
) {
    var ssid by remember {
        mutableStateOf(
            TextFieldValue(
                text = rule.ssid,
                selection = TextRange(rule.ssid.length),
            ),
        )
    }

    var selectedStatus by remember { mutableStateOf(rule.status) }
    var selectedMode by remember { mutableStateOf(rule.mode) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            AddEditRule(
                ssid = ssid,
                onSsidChange = { ssid = it },
                status = selectedStatus,
                onStatusChange = { selectedStatus = it },
                mode = selectedMode,
                onModeChange = { selectedMode = it },
                onAccepted = {
                    onEdit(
                        rule.copy(
                            ssid = ssid.text,
                            status = selectedStatus,
                            mode = selectedMode,
                        ),
                    )
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun WifiRuleAddDialog(onDismiss: () -> Unit, onAdd: (WifiMonitoringModeRule) -> Unit) {
    var ssid by remember { mutableStateOf(TextFieldValue()) }

    var selectedStatus by remember { mutableStateOf(WifiMonitoringModeRule.Status.CONNECTED) }
    var selectedMode by remember { mutableStateOf(MonitoringMode.Off) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            AddEditRule(
                ssid = ssid,
                onSsidChange = { ssid = it },
                status = selectedStatus,
                onStatusChange = { selectedStatus = it },
                mode = selectedMode,
                onModeChange = { selectedMode = it },
                onAccepted = {
                    onAdd(
                        WifiMonitoringModeRule(
                            ssid = ssid.text,
                            status = selectedStatus,
                            mode = selectedMode,
                        ),
                    )
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun AddEditRule(
    ssid: TextFieldValue,
    onSsidChange: (TextFieldValue) -> Unit,
    status: WifiMonitoringModeRule.Status,
    onStatusChange: (WifiMonitoringModeRule.Status) -> Unit,
    mode: MonitoringMode,
    onModeChange: (MonitoringMode) -> Unit,
    onAccepted: () -> Unit,
) {
    val context = LocalContext.current
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    var isModeDropdownExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = ssid,
            onValueChange = onSsidChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.wifi_rule_ssid))
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = status.label(context),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable { isStatusDropdownExpanded = true },
        )
        DropdownMenu(
            expanded = isStatusDropdownExpanded,
            onDismissRequest = { isStatusDropdownExpanded = false },
        ) {
            WifiMonitoringModeRule.Status
                .entries
                .filter { it != WifiMonitoringModeRule.Status.UNKNOWN }
                .forEach {
                    DropdownMenuItem(
                        text = {
                            Text(text = it.label(context))
                        },
                        leadingIcon = {
                            val icon = if (status == it) {
                                Icons.Default.RadioButtonChecked
                            } else {
                                Icons.Default.RadioButtonUnchecked
                            }
                            Icon(icon, null)
                        },
                        onClick = {
                            isStatusDropdownExpanded = false
                            onStatusChange(it)
                        },
                    )
                }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = mode.label(context),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable { isModeDropdownExpanded = true },
        )
        DropdownMenu(
            expanded = isModeDropdownExpanded,
            onDismissRequest = { isModeDropdownExpanded = false },
        ) {
            MonitoringMode.entries.forEach {
                DropdownMenuItem(
                    text = {
                        Text(text = it.label(context))
                    },
                    leadingIcon = {
                        val icon = if (mode == it) {
                            Icons.Default.RadioButtonChecked
                        } else {
                            Icons.Default.RadioButtonUnchecked
                        }
                        Icon(icon, null)
                    },
                    onClick = {
                        isModeDropdownExpanded = false
                        onModeChange(it)
                    },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onAccepted,
                enabled = ssid.text.isNotBlank(),
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    }
}
