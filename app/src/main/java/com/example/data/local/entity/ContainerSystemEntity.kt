package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "container_systems")
data class ContainerSystemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val flavor: String, // e.g. "Debian", "Ubuntu", "Alpine", "Arch", "Void", "Docker"
    val architecture: String = "aarch64",
    val engineType: String = "Podman Rootless", // "Podman Rootless", "PRoot-Distro", "QEMU MicroVM"
    val isInstalled: Boolean = false,
    val isRunning: Boolean = false,
    val isRecommended: Boolean = false,
    val isOfficialImage: Boolean = true,
    val desktopEnv: String = "None", // "Terminal-first", "Xfce desktop", "GNOME desktop", "KDE desktop", "Cinnamon"
    val memoryLimitMb: Int = 1024,
    val cpuShares: Int = 2,
    val diskUsageMb: Long = 0,
    val portMappings: String = "8080:80,2222:22",
    val neonSimdEnabled: Boolean = true,
    val rootfsPath: String = "/data/data/com.example/files/rootfs/",
    val imageTag: String = "latest",
    val registrySource: String = "Docker Hub",
    val statusBadge: String = "Ready",
    val author: String = "Official",
    val downloads: String = "1.2M",
    val likes: String = "12.4k",
    val popularity: String = "98%",
    val installedTimestamp: Long = System.currentTimeMillis()
)
