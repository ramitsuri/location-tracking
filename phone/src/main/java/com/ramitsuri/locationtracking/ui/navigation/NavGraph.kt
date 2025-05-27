package com.ramitsuri.locationtracking.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ramitsuri.locationtracking.ui.AppTheme
import com.ramitsuri.locationtracking.ui.logs.LogsScreen
import com.ramitsuri.locationtracking.ui.home.HomeScreen
import com.ramitsuri.locationtracking.ui.home.HomeViewModel
import com.ramitsuri.locationtracking.ui.logs.LogScreenViewModel
import com.ramitsuri.locationtracking.ui.settings.SettingsScreen
import com.ramitsuri.locationtracking.ui.settings.SettingsViewModel
import com.ramitsuri.locationtracking.ui.wifirule.WifiRulesScreen
import com.ramitsuri.locationtracking.ui.wifirule.WifiRulesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onKillApp: () -> Unit,
    onSingleLocation: () -> Unit,
    onNavToSystemSettings: () -> Unit,
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
                    top = innerPadding.calculateTopPadding(),
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
                        onUpload = viewModel::onUpload,
                        onSingleLocation = onSingleLocation,
                        onNavToSystemSettings = onNavToSystemSettings,
                        onSelectDateTimeForLocations = viewModel::dateTimeSelectedForLocations,
                        onClearDate = viewModel::clearDateForLocations,
                        onLocationSelected = viewModel::selectLocation,
                        onClearSelectedLocation = viewModel::clearSelectedLocation,
                        onSetLocationsViewMode = viewModel::setLocationsViewMode,
                        modifier = Modifier,
                    )
                }

                composable<Destination.Settings> {
                    val viewModel = koinViewModel<SettingsViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        viewState = viewState,
                        onBaseUrlChange = viewModel::setBaseUrl,
                        onDeviceNameChange = viewModel::setDeviceName,
                        onMinAccuracyForDisplayChange = viewModel::setMinAccuracyForDisplay,
                        onKillApp = onKillApp,
                        onNavBack = {
                            navController.navigateUp()
                        },
                        onServiceStart = onServiceStart,
                        onServiceStop = onServiceStop,
                        onNavToWifiRules = {
                            navController.navigate(Destination.WifiRules)
                        },
                        onNavToLogs = {
                            navController.navigate(Destination.Logs)
                        },
                        modifier = Modifier
                            .statusBarsPadding()
                            .displayCutoutPadding(),
                    )
                }

                composable<Destination.WifiRules> {
                    val viewModel = koinViewModel<WifiRulesViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    WifiRulesScreen(
                        viewState = viewState,
                        onNavBack = {
                            navController.navigateUp()
                        },
                        onDelete = viewModel::delete,
                        onEdit = viewModel::edit,
                        onAdd = viewModel::add,
                        modifier = Modifier
                            .statusBarsPadding()
                            .displayCutoutPadding(),
                    )
                }

                composable<Destination.Logs> {
                    val viewModel = koinViewModel<LogScreenViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    LogsScreen(
                        viewState = viewState,
                        onNavBack = {
                            navController.navigateUp()
                        },
                        onSelectAllTags = viewModel::selectAllTagsClicked,
                        onUnselectAllTags = viewModel::unselectAllTagsClicked,
                        onTagClick = viewModel::tagClicked,
                        onClearLogs = viewModel::clearLogsClicked,
                        onLevelClicked = viewModel::levelClicked,
                        modifier = Modifier
                            .statusBarsPadding()
                            .displayCutoutPadding(),
                    )
                }
            }
        }
    }
}
