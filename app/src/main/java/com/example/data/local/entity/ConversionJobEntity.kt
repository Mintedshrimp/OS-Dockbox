package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_jobs")
data class ConversionJobEntity(
    @PrimaryKey val id: String,
    val sourceFileName: String,
    val sourceFormat: String, // "ISO", "VMDK", "VHD", "QCOW2"
    val targetImageName: String,
    val targetTag: String = "latest",
    val targetRegistry: String = "docker.io/library",
    val status: String = "PENDING", // "PENDING", "EXTRACTING", "STRIPPING_KERNEL", "PACKING_OCI", "COMPLETED", "FAILED"
    val progress: Float = 0f,
    val originalSizeMb: Double = 640.0,
    val compressedSizeMb: Double = 180.0,
    val neonSimdSpeedup: String = "3.4x",
    val rootfsFilesExtracted: Int = 0,
    val logOutput: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
