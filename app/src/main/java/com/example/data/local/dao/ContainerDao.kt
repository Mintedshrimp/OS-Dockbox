package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ContainerAppEntity
import com.example.data.local.entity.ContainerSystemEntity
import com.example.data.local.entity.ConversionJobEntity
import com.example.data.local.entity.SupervisorLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {

    // Container Systems
    @Query("SELECT * FROM container_systems ORDER BY isInstalled DESC, isRecommended DESC, name ASC")
    fun getAllSystems(): Flow<List<ContainerSystemEntity>>

    @Query("SELECT * FROM container_systems WHERE id = :id LIMIT 1")
    suspend fun getSystemById(id: String): ContainerSystemEntity?

    @Query("SELECT * FROM container_systems WHERE isInstalled = 1")
    fun getInstalledSystems(): Flow<List<ContainerSystemEntity>>

    @Query("SELECT * FROM container_systems WHERE isRunning = 1")
    fun getRunningSystems(): Flow<List<ContainerSystemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystems(systems: List<ContainerSystemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystem(system: ContainerSystemEntity)

    @Update
    suspend fun updateSystem(system: ContainerSystemEntity)

    @Query("UPDATE container_systems SET isRunning = :isRunning WHERE id = :id")
    suspend fun setSystemRunning(id: String, isRunning: Boolean)

    @Query("UPDATE container_systems SET isInstalled = :isInstalled, diskUsageMb = :diskUsage WHERE id = :id")
    suspend fun setSystemInstalled(id: String, isInstalled: Boolean, diskUsage: Long)

    @Query("DELETE FROM container_systems WHERE id = :id")
    suspend fun deleteSystem(id: String)

    // Apps
    @Query("SELECT * FROM container_apps WHERE containerId = :containerId ORDER BY name ASC")
    fun getAppsForContainer(containerId: String): Flow<List<ContainerAppEntity>>

    @Query("SELECT * FROM container_apps ORDER BY name ASC")
    fun getAllApps(): Flow<List<ContainerAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<ContainerAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: ContainerAppEntity)

    @Update
    suspend fun updateApp(app: ContainerAppEntity)

    @Query("UPDATE container_apps SET isRunning = :isRunning WHERE id = :id")
    suspend fun setAppRunning(id: String, isRunning: Boolean)

    @Query("DELETE FROM container_apps WHERE id = :id")
    suspend fun deleteApp(id: String)

    // Conversions
    @Query("SELECT * FROM conversion_jobs ORDER BY timestamp DESC")
    fun getAllConversions(): Flow<List<ConversionJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversion(job: ConversionJobEntity)

    @Update
    suspend fun updateConversion(job: ConversionJobEntity)

    @Query("DELETE FROM conversion_jobs WHERE id = :id")
    suspend fun deleteConversion(id: String)

    // Logs / Supervisor Journal
    @Query("SELECT * FROM supervisor_logs ORDER BY epochMillis DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<SupervisorLogEntity>>

    @Insert
    suspend fun insertLog(log: SupervisorLogEntity)

    @Insert
    suspend fun insertLogs(logs: List<SupervisorLogEntity>)

    @Query("DELETE FROM supervisor_logs")
    suspend fun clearLogs()
}
