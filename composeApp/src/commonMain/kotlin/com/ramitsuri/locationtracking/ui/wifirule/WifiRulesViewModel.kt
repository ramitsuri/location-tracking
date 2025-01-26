package com.ramitsuri.locationtracking.ui.wifirule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.data.dao.WifiMonitoringModeRuleDao
import com.ramitsuri.locationtracking.model.WifiMonitoringModeRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WifiRulesViewModel(
    private val wifiMonitoringModeRuleDao: WifiMonitoringModeRuleDao,
) : ViewModel() {

    val viewState = wifiMonitoringModeRuleDao
        .getAll()
        .map { rules ->
            WifiRulesViewState(rules.filter { it.status != WifiMonitoringModeRule.Status.UNKNOWN })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WifiRulesViewState(),
        )

    fun add(rule: WifiMonitoringModeRule) {
        viewModelScope.launch {
            wifiMonitoringModeRuleDao.insert(rule)
        }
    }

    fun delete(rule: WifiMonitoringModeRule) {
        viewModelScope.launch {
            wifiMonitoringModeRuleDao.delete(rule)
        }
    }

    fun edit(rule: WifiMonitoringModeRule) {
        viewModelScope.launch {
            wifiMonitoringModeRuleDao.update(rule)
        }
    }
}
