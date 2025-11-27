package com.ramitsuri.locationtracking.ui.seenwifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.data.dao.SeenWifiDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SeenWifiViewModel(
    private val seenWifiDao: SeenWifiDao,
) : ViewModel() {
    private val searchText = MutableStateFlow("")

    val viewState = searchText
        .flatMapLatest { text ->
            seenWifiDao
                .getFlow(query = text)
                .map { wifiList ->
                    SeenWifiState(
                        searchText = text,
                        seenWifiList = wifiList,
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SeenWifiState(),
        )

    fun onSearchTextChange(text: String) {
        searchText.value = text
    }

    fun toggleFavorite(ssid: String) {
        val wifi = viewState.value.seenWifiList.find { it.ssid == ssid } ?: return
        viewModelScope.launch {
            seenWifiDao.update(wifi.copy(isFavorite = !wifi.isFavorite))
        }
    }
}
