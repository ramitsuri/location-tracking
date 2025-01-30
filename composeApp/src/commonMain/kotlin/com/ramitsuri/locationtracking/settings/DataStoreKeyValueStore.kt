package com.ramitsuri.locationtracking.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path

internal class DataStoreKeyValueStore(
    dataStorePathProducer: () -> Path,
) : KeyValueStore {
    private val dataStore =
        PreferenceDataStoreFactory.createWithPath(produceFile = dataStorePathProducer)

    override fun getStringFlow(key: Key, defaultValue: String?): Flow<String?> {
        return dataStore
            .data
            .map {
                it[stringPreferencesKey(key.value)] ?: defaultValue
            }
            .distinctUntilChanged()
    }

    override suspend fun getString(key: Key, defaultValue: String?): String? {
        return getStringFlow(key, defaultValue).first()
    }

    override suspend fun putString(key: Key, value: String?) {
        if (value == null) {
            remove(stringPreferencesKey(key.value))
        } else {
            dataStore.edit {
                it[stringPreferencesKey(key.value)] = value
            }
        }
    }

    private suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit {
            it.remove(key)
        }
    }
}
