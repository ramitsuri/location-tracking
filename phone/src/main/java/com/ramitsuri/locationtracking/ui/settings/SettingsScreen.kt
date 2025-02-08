package com.ramitsuri.locationtracking.ui.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramitsuri.locationtracking.R

@Composable
fun SettingsScreen(
    viewState: SettingsViewState,
    modifier: Modifier = Modifier,
    onNavBack: () -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onDeviceNameChange: (String) -> Unit,
    onMinAccuracyForDisplayChange: (Int) -> Unit,
    onKillApp: () -> Unit,
    onServiceStart: () -> Unit,
    onServiceStop: () -> Unit,
    onNavToWifiRules: () -> Unit,
    onNavToLogs: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
        }
        TextField(
            text = viewState.baseUrl,
            label = stringResource(id = R.string.url_hint),
            enabled = !viewState.isUploadWorkerRunning,
            onTextSet = onBaseUrlChange,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            text = viewState.deviceName,
            label = stringResource(R.string.device_name_hint),
            enabled = !viewState.isUploadWorkerRunning,
            onTextSet = onDeviceNameChange,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            text = viewState.minAccuracyForDisplay.toString(),
            label = stringResource(R.string.min_accuracy_for_display_hint),
            enabled = true,
            onTextSet = { onMinAccuracyForDisplayChange(it.toIntOrNull() ?: 0) },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNavToWifiRules,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.wifi_rules))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNavToLogs,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.logs))
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth()) {
            if (viewState.isServiceRunning) {
                Button(
                    onClick = onServiceStop,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.stop_service))
                }
            } else {
                Button(
                    onClick = onServiceStart,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.start_service))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onKillApp,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.kill_app))
            }
        }
    }
}

@Composable
private fun TextField(text: String, label: String, enabled: Boolean, onTextSet: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = { showDialog = true })
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        val annotatedText = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(label)
            }
            append(text)
        }
        Text(
            text = annotatedText,
            color = if (enabled) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            },
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                ) {
                    var inputValue by remember {
                        mutableStateOf(
                            TextFieldValue(
                                text = text,
                                selection = TextRange(text.length),
                            ),
                        )
                    }
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(label)
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = {
                                onTextSet(inputValue.text)
                                showDialog = false
                            },
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }
}
