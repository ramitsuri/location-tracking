package com.ramitsuri.locationtracking.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ramitsuri.locationtracking.ui.AppTheme
import com.ramitsuri.locationtracking.ui.home.HomeScreen
import com.ramitsuri.locationtracking.ui.home.HomeViewModel
import com.ramitsuri.locationtracking.ui.settings.SettingsScreen
import com.ramitsuri.locationtracking.ui.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onKillApp: () -> Unit,
    onSingleLocation: () -> Unit,
    onNavToAppSettings: () -> Unit,
    onServiceStart: () -> Unit,
    onServiceStop: () -> Unit,
) {
    AppTheme {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier =
            modifier
                .fillMaxSize(),
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destination.Home,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding().plus(16.dp),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
            ) {
                composable<Destination.Home> {
                    val viewModel = koinViewModel<HomeViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    HomeScreen(
                        viewState = viewState,
                        onNavToSettings = {
                            navController.navigate(Destination.Settings)
                        },
                        onSingleLocation = onSingleLocation,
                        onNavToAppSettings = onNavToAppSettings,
                    )
                }

                composable<Destination.Settings> {
                    val viewModel = koinViewModel<SettingsViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        viewState = viewState,
                        onBaseUrlChange = viewModel::setBaseUrl,
                        onDeviceNameChange = viewModel::setDeviceName,
                        onKillApp = onKillApp,
                        onNavBack = {
                            navController.navigateUp()
                        },
                        onUpload = viewModel::onUpload,
                        onServiceStart = onServiceStart,
                        onServiceStop = onServiceStop,
                    )
                }
            }
        }
    }
}
