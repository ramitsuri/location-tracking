package com.ramitsuri.locationtracking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
        super.onCreate(savedInstanceState)
        startService()
        permissionMonitor.monitorPermissions(Permission.entries)
        setContent {
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
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
                Spacer(modifier = Modifier.height(32.dp))
                Text(stringResource(R.string.current_mode, mode.label()))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.locations, locations.toString()))
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
