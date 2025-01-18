package com.ramitsuri.locationtracking.settings

import kotlinx.coroutines.flow.Flow

internal interface KeyValueStore {
    fun getStringFlow(key: Key, defaultValue: String?): Flow<String?>

    suspend fun getString(key: Key, defaultValue: String?): String?

    suspend fun putString(key: Key, value: String?)
}
