package com.example.data.repository

import com.example.data.local.dao.ContainerDao
import com.example.data.local.entity.ContainerAppEntity
import com.example.data.local.entity.ContainerSystemEntity
import com.example.data.local.entity.ConversionJobEntity
import com.example.data.local.entity.SupervisorLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ContainerRepository(private val dao: ContainerDao) {

    val allSystems: Flow<List<ContainerSystemEntity>> = dao.getAllSystems()
    val installedSystems: Flow<List<ContainerSystemEntity>> = dao.getInstalledSystems()
    val runningSystems: Flow<List<ContainerSystemEntity>> = dao.getRunningSystems()
    val allApps: Flow<List<ContainerAppEntity>> = dao.getAllApps()
    val conversions: Flow<List<ConversionJobEntity>> = dao.getAllConversions()
    val supervisorLogs: Flow<List<SupervisorLogEntity>> = dao.getRecentLogs()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    suspend fun initializeDefaultsIfNeeded() {
        val existing = allSystems.firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaultSystems = listOf(
                ContainerSystemEntity(
                    id = "debian-trixie",
                    name = "Debian 13 (Trixie)",
                    version = "13 (Trixie)",
                    flavor = "Debian",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = true,
                    isRunning = true,
                    isRecommended = false,
                    desktopEnv = "Cinnamon",
                    diskUsageMb = 840,
                    portMappings = "8080:80, 2222:22, 5901:5901",
                    neonSimdEnabled = true,
                    statusBadge = "Active"
                ),
                ContainerSystemEntity(
                    id = "ubuntu-2204-lts",
                    name = "Ubuntu 22.04 LTS",
                    version = "22.04.4 LTS (Jammy)",
                    flavor = "Ubuntu",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    isRecommended = true,
                    desktopEnv = "Terminal-first",
                    diskUsageMb = 0,
                    portMappings = "8081:80",
                    neonSimdEnabled = true,
                    statusBadge = "Recommended"
                ),
                ContainerSystemEntity(
                    id = "alpine-322",
                    name = "Alpine Linux 3.22",
                    version = "3.22.0 (Musl)",
                    flavor = "Alpine",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    isRecommended = false,
                    desktopEnv = "Terminal-first",
                    diskUsageMb = 0,
                    portMappings = "3000:3000",
                    neonSimdEnabled = true,
                    statusBadge = "Ultra-light"
                ),
                ContainerSystemEntity(
                    id = "arch-linux",
                    name = "Arch Linux",
                    version = "Rolling (Latest)",
                    flavor = "Arch",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    isRecommended = false,
                    desktopEnv = "Terminal-first",
                    diskUsageMb = 0,
                    portMappings = "8000:8000",
                    neonSimdEnabled = true,
                    statusBadge = "Rolling"
                ),
                ContainerSystemEntity(
                    id = "ubuntu-2004-lts",
                    name = "Ubuntu 20.04 LTS",
                    version = "20.04.6 LTS (Focal)",
                    flavor = "Ubuntu",
                    architecture = "aarch64",
                    engineType = "PRoot-Distro",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "Terminal-first",
                    neonSimdEnabled = true
                ),
                ContainerSystemEntity(
                    id = "ubuntu-2004-xfce",
                    name = "Ubuntu 20.04 LTS",
                    version = "20.04.6 LTS (Focal)",
                    flavor = "Ubuntu",
                    architecture = "aarch64",
                    engineType = "PRoot-Distro",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "Xfce desktop",
                    neonSimdEnabled = true
                ),
                ContainerSystemEntity(
                    id = "ubuntu-2204-gnome",
                    name = "Ubuntu 22.04 LTS",
                    version = "22.04.4 LTS",
                    flavor = "Ubuntu",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "GNOME desktop",
                    neonSimdEnabled = true
                ),
                ContainerSystemEntity(
                    id = "ubuntu-2204-kde",
                    name = "Ubuntu 22.04 LTS",
                    version = "22.04.4 LTS",
                    flavor = "Ubuntu",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "KDE desktop",
                    neonSimdEnabled = true
                ),
                ContainerSystemEntity(
                    id = "ubuntu-2404-lts",
                    name = "Ubuntu 24.04 LTS",
                    version = "24.04 LTS (Noble)",
                    flavor = "Ubuntu",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "Terminal-first",
                    neonSimdEnabled = true
                ),
                ContainerSystemEntity(
                    id = "ubuntu-2604-lts",
                    name = "Ubuntu 26.04 LTS",
                    version = "26.04 LTS (Plasma Preview)",
                    flavor = "Ubuntu",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "plasma",
                    neonSimdEnabled = true
                ),
                ContainerSystemEntity(
                    id = "void-linux",
                    name = "Void Linux",
                    version = "Musl minimal",
                    flavor = "Void",
                    architecture = "aarch64",
                    engineType = "PRoot-Distro",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "Terminal-first",
                    neonSimdEnabled = true
                ),
                ContainerSystemEntity(
                    id = "docker-nginx-alpine",
                    name = "Nginx Web Server",
                    version = "1.27-alpine",
                    flavor = "Docker",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "Web Service",
                    portMappings = "80:80",
                    neonSimdEnabled = true
                ),
                ContainerSystemEntity(
                    id = "docker-python-slim",
                    name = "Python AI & Jupyter Lab",
                    version = "3.12-slim",
                    flavor = "Docker",
                    architecture = "aarch64",
                    engineType = "Podman Rootless",
                    isInstalled = false,
                    isRunning = false,
                    desktopEnv = "Web Service",
                    portMappings = "8888:8888",
                    neonSimdEnabled = true
                )
            )
            dao.insertSystems(defaultSystems)

            // Seed default apps for debian-trixie
            val defaultApps = listOf(
                ContainerAppEntity(
                    id = "app-network",
                    containerId = "debian-trixie",
                    name = "Advanced Network Configuration",
                    category = "System",
                    command = "nm-connection-editor",
                    displayType = "DISPLAY :0",
                    iconName = "settings"
                ),
                ContainerAppEntity(
                    id = "app-chromium",
                    containerId = "debian-trixie",
                    name = "Chromium Web Browser",
                    category = "Web",
                    command = "chromium --no-sandbox",
                    displayType = "DISPLAY :0",
                    iconName = "browser",
                    isRunning = true
                ),
                ContainerAppEntity(
                    id = "app-files",
                    containerId = "debian-trixie",
                    name = "Files",
                    category = "System",
                    command = "nautilus --new-window",
                    displayType = "DISPLAY :0",
                    iconName = "files"
                ),
                ContainerAppEntity(
                    id = "app-print",
                    containerId = "debian-trixie",
                    name = "Print Settings",
                    category = "System",
                    command = "system-config-printer",
                    displayType = "DISPLAY :0",
                    iconName = "print"
                ),
                ContainerAppEntity(
                    id = "app-uxterm",
                    containerId = "debian-trixie",
                    name = "UXTerm",
                    category = "Development",
                    command = "uxterm -fa 'Monospace' -fs 12",
                    displayType = "DISPLAY :0",
                    iconName = "terminal"
                ),
                ContainerAppEntity(
                    id = "app-xterm",
                    containerId = "debian-trixie",
                    name = "XTerm",
                    category = "Development",
                    command = "xterm",
                    displayType = "DISPLAY :0",
                    iconName = "terminal"
                ),
                ContainerAppEntity(
                    id = "app-vscode",
                    containerId = "debian-trixie",
                    name = "VS Code Server",
                    category = "Development",
                    command = "code-server --bind-addr 127.0.0.1:8080 --auth none",
                    displayType = "PORT 8080",
                    iconName = "code"
                ),
                ContainerAppEntity(
                    id = "app-htop",
                    containerId = "debian-trixie",
                    name = "HTOP System Monitor",
                    category = "Utility",
                    command = "htop",
                    displayType = "CLI Terminal",
                    iconName = "monitor"
                )
            )
            dao.insertApps(defaultApps)

            // Seed default conversion job
            val sampleJob = ConversionJobEntity(
                id = UUID.randomUUID().toString(),
                sourceFileName = "alpine-virt-3.22-aarch64.iso",
                sourceFormat = "ISO",
                targetImageName = "alpine-custom-oci",
                targetTag = "v1.0",
                targetRegistry = "docker.io/library",
                status = "COMPLETED",
                progress = 1.0f,
                originalSizeMb = 145.0,
                compressedSizeMb = 48.2,
                neonSimdSpeedup = "3.8x",
                rootfsFilesExtracted = 14208,
                logOutput = "[ISO-PARSER] Found bootable ISO 9660\n[EXTRACT] Extracted squashfs rootfs (14,208 inodes)\n[NEON-SIMD] Vectorized zstd layer compression completed at 480 MB/s\n[OCI-GEN] Generated OCI Manifest v1.1.0\n[PODMAN] Tagged as localhost/alpine-custom-oci:v1.0"
            )
            dao.insertConversion(sampleJob)

            // Seed initial Supervisor Journal logs
            val initialLogs = listOf(
                SupervisorLogEntity(
                    eventTag = "desktop_start_requested",
                    message = "Starting Cinnamon (Software Rendering) for proot-debian-trixie",
                    level = "SUCCESS",
                    timestamp = timeFormat.format(Date(System.currentTimeMillis() - 120000))
                ),
                SupervisorLogEntity(
                    eventTag = "neon_simd_vector_accelerator_online",
                    message = "ARMv8.2 NEON SIMD 128-bit pipeline initialized with fast-path memory copy",
                    level = "SUCCESS",
                    timestamp = timeFormat.format(Date(System.currentTimeMillis() - 180000))
                ),
                SupervisorLogEntity(
                    eventTag = "podman_engine_init",
                    message = "Rootless container engine initialized with crun OCI runtime (UID map: 100000:65536)",
                    level = "INFO",
                    timestamp = timeFormat.format(Date(System.currentTimeMillis() - 240000))
                ),
                SupervisorLogEntity(
                    eventTag = "seccomp_bpf_filter_loaded",
                    message = "Bypass seccomp syscall filter applied for unprivileged clone() and setns()",
                    level = "INFO",
                    timestamp = timeFormat.format(Date(System.currentTimeMillis() - 300000))
                ),
                SupervisorLogEntity(
                    eventTag = "storage_driver_overlayfs",
                    message = "Overlayfs mounted on /data/data/com.example/files/storage/overlay",
                    level = "INFO",
                    timestamp = timeFormat.format(Date(System.currentTimeMillis() - 360000))
                )
            )
            dao.insertLogs(initialLogs)
        }
    }

    suspend fun logEvent(tag: String, message: String, level: String = "INFO") {
        dao.insertLog(
            SupervisorLogEntity(
                eventTag = tag,
                message = message,
                level = level,
                timestamp = timeFormat.format(Date())
            )
        )
    }

    suspend fun toggleSystemRunning(id: String, currentlyRunning: Boolean) {
        val targetState = !currentlyRunning
        dao.setSystemRunning(id, targetState)
        if (targetState) {
            logEvent("container_start", "Container $id started with rootless podman namespace", "SUCCESS")
        } else {
            logEvent("container_stop", "Container $id gracefully terminated", "INFO")
        }
    }

    suspend fun toggleAppRunning(id: String, currentlyRunning: Boolean) {
        val targetState = !currentlyRunning
        dao.setAppRunning(id, targetState)
        if (targetState) {
            logEvent("app_launched", "Launched container application [$id]", "SUCCESS")
        } else {
            logEvent("app_terminated", "Application [$id] stopped", "INFO")
        }
    }

    suspend fun installSystem(systemId: String, scope: CoroutineScope, onProgress: (Float, String) -> Unit) {
        logEvent("oci_pull_start", "Pulling layer image for $systemId from registry", "INFO")
        val steps = listOf(
            0.15f to "Resolving manifest from registry (docker.io / ghcr.io)...",
            0.35f to "Pulling OCI rootfs layers (NEON SIMD decompression active)...",
            0.65f to "Extracting rootfs filesystem structure...",
            0.85f to "Configuring rootless user namespaces & permissions...",
            1.0f to "Installation complete! Podman container ready."
        )

        for ((progress, msg) in steps) {
            delay(400)
            onProgress(progress, msg)
        }

        dao.setSystemInstalled(systemId, true, 680)
        logEvent("system_installed", "Successfully provisioned container image $systemId", "SUCCESS")
    }

    suspend fun deleteSystem(systemId: String) {
        dao.setSystemInstalled(systemId, false, 0)
        dao.setSystemRunning(systemId, false)
        logEvent("container_deleted", "Removed container and rootfs for $systemId", "WARN")
    }

    suspend fun updateSystem(system: ContainerSystemEntity) {
        dao.updateSystem(system)
    }

    suspend fun addCustomContainer(
        name: String,
        imageRef: String,
        engine: String,
        ports: String,
        simd: Boolean
    ) {
        val id = "custom-" + name.lowercase().replace(" ", "-") + "-" + System.currentTimeMillis() % 1000
        val newSystem = ContainerSystemEntity(
            id = id,
            name = name,
            version = imageRef,
            flavor = if (imageRef.contains("alpine")) "Alpine" else if (imageRef.contains("ubuntu")) "Ubuntu" else "Docker",
            architecture = "aarch64",
            engineType = engine,
            isInstalled = true,
            isRunning = true,
            isRecommended = false,
            desktopEnv = "CLI / Port Forwarded",
            diskUsageMb = 240,
            portMappings = ports,
            neonSimdEnabled = simd,
            statusBadge = "Custom"
        )
        dao.insertSystem(newSystem)
        logEvent("container_created", "Created container $name ($imageRef) using $engine", "SUCCESS")
    }

    suspend fun runConversion(
        sourceFileName: String,
        sourceFormat: String,
        targetImageName: String,
        targetRegistry: String,
        useSimd: Boolean,
        onUpdate: (ConversionJobEntity) -> Unit
    ) {
        val id = UUID.randomUUID().toString()
        var job = ConversionJobEntity(
            id = id,
            sourceFileName = sourceFileName,
            sourceFormat = sourceFormat,
            targetImageName = targetImageName,
            targetTag = "latest",
            targetRegistry = targetRegistry,
            status = "EXTRACTING",
            progress = 0.1f,
            originalSizeMb = if (sourceFormat == "ISO") 720.0 else 1850.0,
            compressedSizeMb = if (sourceFormat == "ISO") 195.0 else 420.0,
            neonSimdSpeedup = if (useSimd) "3.6x" else "1.0x",
            rootfsFilesExtracted = 2400,
            logOutput = "[ISO-VMDK PARSER] Inspecting $sourceFileName format: $sourceFormat\n[MBR/GPT] Found primary root Linux partition"
        )
        dao.insertConversion(job)
        onUpdate(job)
        logEvent("conversion_started", "Started conversion for $sourceFileName -> $targetImageName", "INFO")

        delay(600)
        job = job.copy(
            status = "STRIPPING_KERNEL",
            progress = 0.45f,
            rootfsFilesExtracted = 18450,
            logOutput = job.logOutput + "\n[STRIP] Removed vmlinuz, initrd, bootloader EFI\n[NEON-SIMD] Vectorized memory copy active during tree walk"
        )
        dao.updateConversion(job)
        onUpdate(job)

        delay(600)
        job = job.copy(
            status = "PACKING_OCI",
            progress = 0.8f,
            rootfsFilesExtracted = 31200,
            logOutput = job.logOutput + "\n[OCI-TAR] Creating layer tarball with zstd level 6\n[SIMD SPEEDUP] Decompression/compression rate: 460 MB/s (NEON SIMD)\n[MANIFEST] Generated OCI image-spec config.json"
        )
        dao.updateConversion(job)
        onUpdate(job)

        delay(500)
        job = job.copy(
            status = "COMPLETED",
            progress = 1.0f,
            logOutput = job.logOutput + "\n[PODMAN] Successfully imported image localhost/$targetImageName:latest\n[REGISTRY] Ready to publish to $targetRegistry/$targetImageName:latest"
        )
        dao.updateConversion(job)
        onUpdate(job)

        // Automatically add to container systems
        val convertedSystem = ContainerSystemEntity(
            id = "converted-" + targetImageName.lowercase(),
            name = "$targetImageName ($sourceFormat)",
            version = "Converted OCI Image",
            flavor = "Converted",
            architecture = "aarch64",
            engineType = "Podman Rootless",
            isInstalled = true,
            isRunning = false,
            isRecommended = false,
            desktopEnv = "Terminal-first",
            diskUsageMb = job.compressedSizeMb.toLong(),
            portMappings = "8080:80",
            neonSimdEnabled = useSimd,
            statusBadge = "Converted"
        )
        dao.insertSystem(convertedSystem)
        logEvent("conversion_completed", "Converted $sourceFileName to container $targetImageName:latest", "SUCCESS")
    }

    suspend fun clearLogs() {
        dao.clearLogs()
        logEvent("journal_cleared", "Supervisor journal logs reset by user", "INFO")
    }
}
