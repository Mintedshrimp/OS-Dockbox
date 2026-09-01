package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supervisor_logs")
data class SupervisorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventTag: String, // e.g. "desktop_start_requested", "podman_engine_init", "simd_vector_ready", "oci_pull_layer"
    val message: String,
    val level: String = "INFO", // "INFO", "SUCCESS", "WARN", "ERROR"
    val timestamp: String,
    val epochMillis: Long = System.currentTimeMillis()
)
