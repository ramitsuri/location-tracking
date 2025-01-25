package com.ramitsuri.locationtracking.testutils

import com.ramitsuri.locationtracking.settings.Key
import com.ramitsuri.locationtracking.settings.KeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestKeyValueStore : KeyValueStore {
    private val values = mutableMapOf<Key, Any?>()

    override fun getStringFlow(key: Key, defaultValue: String?): Flow<String?> {
        return flow {
            emit(values[key] as? String?)
        }
    }

    override suspend fun getString(key: Key, defaultValue: String?): String? {
        return values[key] as? String?
    }

    override suspend fun putString(key: Key, value: String?) {
        values[key] = value
    }
}
