package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "container_apps")
data class ContainerAppEntity(
    @PrimaryKey val id: String,
    val containerId: String,
    val name: String,
    val category: String, // "System", "Web", "Development", "Utility", "CLI"
    val command: String,
    val displayType: String = "DISPLAY :0", // "DISPLAY :0", "CLI Terminal", "PORT 8080 (Web)"
    val iconName: String = "default",
    val isRunning: Boolean = false,
    val isUserApp: Boolean = false, // true = User installed, false = System / Core distro package
    val description: String = "",
    val memoryUsageMb: Int = 45,
    val port: Int = 0
)
