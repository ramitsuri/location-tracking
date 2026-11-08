package com.ramitsuri.locationtracking.ui.seenwifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.locationtracking.data.dao.SeenWifiDao
import com.ramitsuri.locationtracking.model.WifiSortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SeenWifiViewModel(
    private val seenWifiDao: SeenWifiDao,
) : ViewModel() {
    private val searchText = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(WifiSortOrder.MOST_TIMES)

    val viewState = combine(
        searchText,
        sortOrder,
    ) { text, sort ->
        text to sort
    }.flatMapLatest { (text, sort) ->
        seenWifiDao
            .getFlow(query = text, sortOrder = sort)
            .map { wifiList ->
                SeenWifiState(
                    searchText = text,
                    sortOrder = sort,
                    seenWifiList = wifiList,
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SeenWifiState(),
    )

    fun onSearchTextChange(text: String) {
        searchText.value = text
    }

    fun onSortOrderChange(sortOrder: WifiSortOrder) {
        this.sortOrder.value = sortOrder
    }

    fun toggleFavorite(ssid: String) {
        val wifi = viewState.value.seenWifiList.find { it.ssid == ssid } ?: return
        viewModelScope.launch {
            seenWifiDao.update(wifi.copy(isFavorite = !wifi.isFavorite))
        }
    }
}
