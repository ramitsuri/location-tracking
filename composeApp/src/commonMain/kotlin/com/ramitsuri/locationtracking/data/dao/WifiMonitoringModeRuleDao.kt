package com.ramitsuri.locationtracking.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ramitsuri.locationtracking.model.WifiMonitoringModeRule
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiMonitoringModeRuleDao {
    @Insert
    suspend fun insert(rule: WifiMonitoringModeRule)

    @Delete
    suspend fun delete(rule: WifiMonitoringModeRule)

    @Query("SELECT * FROM wifi_monitoring_mode_rules WHERE ssid IN (:ssids)")
    suspend fun getAll(ssids: List<String>): List<WifiMonitoringModeRule>

    @Query("SELECT * FROM wifi_monitoring_mode_rules")
    fun getAll(): Flow<List<WifiMonitoringModeRule>>

    @Update
    suspend fun update(rule: WifiMonitoringModeRule)
}
