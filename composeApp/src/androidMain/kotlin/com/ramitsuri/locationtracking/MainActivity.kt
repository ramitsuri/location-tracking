package com.ramitsuri.locationtracking

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ramitsuri.locationtracking.log.logD
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.permissions.AndroidPermissionMonitor
import com.ramitsuri.locationtracking.permissions.Permission
import com.ramitsuri.locationtracking.permissions.PermissionChecker
import com.ramitsuri.locationtracking.repository.LocationRepository
import com.ramitsuri.locationtracking.services.BackgroundService
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.ui.AppTheme
import com.ramitsuri.locationtracking.upload.UploadWorker
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val settings by inject<Settings>()
    private val locationRepository by inject<LocationRepository>()
    private val permissionMonitor by lazy {
        AndroidPermissionMonitor(get<PermissionChecker>(), this@MainActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        startService()
        permissionMonitor.monitorPermissions(Permission.entries)
        enableEdgeToEdge()
        setContent {
            val darkTheme = isSystemInDarkTheme()
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle =
                    SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle =
                    SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
                onDispose {}
            }
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Screen()
                }
            }
        }
    }

    @Composable
    private fun Screen() {
        val mode by settings.getMonitoringMode().collectAsStateWithLifecycle(null)
        val locations by locationRepository
            .getCount()
            .collectAsStateWithLifecycle(0)
        val notGrantedPermissions by permissionMonitor
            .permissionState
            .map { results ->
                results.filter { permissionResult ->
                    !permissionResult.granted
                }
            }
            .collectAsStateWithLifecycle(emptyList())

        val url by settings.getBaseUrlFlow().collectAsStateWithLifecycle("")
        val deviceName by settings.getDeviceNameFlow().collectAsStateWithLifecycle("")
        val isWorkRunning by UploadWorker.isRunning(this).collectAsStateWithLifecycle(false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TextField(
                text = url,
                label = stringResource(R.string.url_hint),
                enabled = !isWorkRunning,
                onTextSet = {
                    lifecycleScope.launch {
                        settings.setBaseUrl(it)
                    }
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                text = deviceName,
                label = stringResource(R.string.device_name_hint),
                enabled = !isWorkRunning,
                onTextSet = {
                    lifecycleScope.launch {
                        settings.setDeviceName(it)
                    }
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = ::killApp,
                ) {
                    Text(stringResource(R.string.kill_app))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = ::stopService,
                ) {
                    Text(stringResource(R.string.stop_service))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = {
                        lifecycleScope.launch {
                            settings.setNextMonitoringMode()
                        }
                    },
                ) {
                    Text(stringResource(R.string.change_mode))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        startService(
                            action = BackgroundService.INTENT_ACTION_SEND_LOCATION_USER,
                        )
                    },
                ) {
                    Text(stringResource(R.string.single_location))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = {
                        if (!isWorkRunning) {
                            UploadWorker.enqueueImmediate(this@MainActivity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isWorkRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(R.string.upload_locations))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            val currentMode = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.current_mode))
                }
                append(mode.label())
            }
            Text(text = currentMode)
            Spacer(modifier = Modifier.height(16.dp))
            val count = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.locations))
                }
                append(locations.toString())
            }
            Text(text = count)
            Spacer(modifier = Modifier.height(16.dp))
            if (notGrantedPermissions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(3f)) {
                        Text(stringResource(R.string.not_granted_permissions))
                        notGrantedPermissions
                            .forEach {
                                Text(it.permission.name)
                            }
                    }
                    IconButton(
                        onClick = ::navToAppSettings,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            }
        }
    }

    @Composable
    private fun TextField(
        text: String,
        label: String,
        enabled: Boolean,
        onTextSet: (String) -> Unit,
    ) {
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

    private fun navToAppSettings() {
        startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            },
        )
    }

    private fun startService(action: String? = null) {
        logD(TAG) { "requesting service start" }
        ContextCompat.startForegroundService(
            this,
            Intent()
                .setClass(this, BackgroundService::class.java)
                .apply {
                    action?.also { this.action = it }
                },
        )
    }

    private fun stopService() {
        logD(TAG) { "requesting service stop" }
        stopService(Intent(this, BackgroundService::class.java))
    }

    private fun killApp() {
        stopService(Intent(this, BackgroundService::class.java))
        finishAffinity()
        Process.killProcess(Process.myPid())
    }

    @Composable
    private fun MonitoringMode?.label() = when (this) {
        MonitoringMode.Quiet -> stringResource(R.string.monitoring_mode_quiet)
        MonitoringMode.Slow -> stringResource(R.string.monitoring_mode_manual)
        MonitoringMode.Significant -> stringResource(R.string.monitoring_mode_significant_changes)
        MonitoringMode.Move -> stringResource(R.string.monitoring_mode_move)
        else -> ""
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:
 * activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;
 * drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:
 * activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;
 * drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
