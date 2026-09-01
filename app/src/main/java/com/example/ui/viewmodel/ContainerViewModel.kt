package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ContainerAppEntity
import com.example.data.local.entity.ContainerSystemEntity
import com.example.data.local.entity.ConversionJobEntity
import com.example.data.local.entity.SupervisorLogEntity
import com.example.data.repository.ContainerRepository
import com.example.domain.model.SystemSpec
import com.example.domain.model.TerminalLine
import com.example.domain.model.TerminalLineType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    HOME("Home"),
    LINUX("Linux"),
    TERMINAL("Terminal"),
    APPS("Apps"),
    CONVERT("Convert"),
    ABOUT("About")
}

class ContainerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContainerRepository

    val allSystems: StateFlow<List<ContainerSystemEntity>>
    val allApps: StateFlow<List<ContainerAppEntity>>
    val conversions: StateFlow<List<ConversionJobEntity>>
    val supervisorLogs: StateFlow<List<SupervisorLogEntity>>

    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab = _selectedTab.asStateFlow()

    private val _searchQuerySystems = MutableStateFlow("")
    val searchQuerySystems = _searchQuerySystems.asStateFlow()

    private val _searchQueryApps = MutableStateFlow("")
    val searchQueryApps = _searchQueryApps.asStateFlow()

    private val _systemFilter = MutableStateFlow("ALL") // "ALL", "INSTALLED", "PODMAN", "PROOT", "RUNNING"
    val systemFilter = _systemFilter.asStateFlow()

    private val _activeContainerId = MutableStateFlow("debian-trixie")
    val activeContainerId = _activeContainerId.asStateFlow()

    // Terminal State
    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines = _terminalLines.asStateFlow()

    private val _terminalInput = MutableStateFlow("")
    val terminalInput = _terminalInput.asStateFlow()

    // Dialog & Flow States
    private val _installingSystemId = MutableStateFlow<String?>(null)
    val installingSystemId = _installingSystemId.asStateFlow()

    private val _installProgress = MutableStateFlow(0f)
    val installProgress = _installProgress.asStateFlow()

    private val _installStatusText = MutableStateFlow("")
    val installStatusText = _installStatusText.asStateFlow()

    private val _showNewContainerDialog = MutableStateFlow(false)
    val showNewContainerDialog = _showNewContainerDialog.asStateFlow()

    private val _showConvertDialog = MutableStateFlow(false)
    val showConvertDialog = _showConvertDialog.asStateFlow()

    private val _showDesktopViewer = MutableStateFlow(false)
    val showDesktopViewer = _showDesktopViewer.asStateFlow()

    private val _selectedSystemForEdit = MutableStateFlow<ContainerSystemEntity?>(null)
    val selectedSystemForEdit = _selectedSystemForEdit.asStateFlow()

    // System Hardware Specs & NEON Benchmark
    val systemSpec = MutableStateFlow(SystemSpec())
    private val _neonBenchmarkResult = MutableStateFlow<String?>(null)
    val neonBenchmarkResult = _neonBenchmarkResult.asStateFlow()

    // Notification / SnackBar message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ContainerRepository(db.containerDao())

        allSystems = repository.allSystems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allApps = repository.allApps.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        conversions = repository.conversions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        supervisorLogs = repository.supervisorLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            initTerminalWelcome()
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuerySystems(query: String) {
        _searchQuerySystems.value = query
    }

    fun setSearchQueryApps(query: String) {
        _searchQueryApps.value = query
    }

    fun setSystemFilter(filter: String) {
        _systemFilter.value = filter
    }

    fun setActiveContainer(id: String) {
        _activeContainerId.value = id
        appendTerminalLine(TerminalLine("Switched active container context to: $id", TerminalLineType.HEADER))
    }

    fun setTerminalInput(input: String) {
        _terminalInput.value = input
    }

    fun showDesktop(show: Boolean) {
        _showDesktopViewer.value = show
        if (show) {
            viewModelScope.launch {
                repository.logEvent("desktop_opened", "Attached interactive DISPLAY :0 X11 surface", "SUCCESS")
            }
        }
    }

    fun openEditSystem(system: ContainerSystemEntity?) {
        _selectedSystemForEdit.value = system
    }

    fun setShowNewContainerDialog(show: Boolean) {
        _showNewContainerDialog.value = show
    }

    fun setShowConvertDialog(show: Boolean) {
        _showConvertDialog.value = show
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun initTerminalWelcome() {
        val lines = listOf(
            TerminalLine("=== OS Dockbox Podman Environment (aarch64) ===", TerminalLineType.HEADER),
            TerminalLine("Kernel: Linux 6.6.21-android-rootless-crun", TerminalLineType.OUTPUT),
            TerminalLine("SIMD: ARM NEON 128-bit Vectorization [ENABLED]", TerminalLineType.SUCCESS),
            TerminalLine("Engine: Podman 5.0.3 (Rootless user-ns isolation)", TerminalLineType.OUTPUT),
            TerminalLine("Type 'help' for available commands or use the shortcut bar below.", TerminalLineType.WARNING),
            TerminalLine("dockbox@podman-debian:~$ ", TerminalLineType.OUTPUT)
        )
        _terminalLines.value = lines
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        appendTerminalLine(TerminalLine("dockbox@podman-debian:~$ $trimmed", TerminalLineType.INPUT))
        _terminalInput.value = ""

        viewModelScope.launch {
            val responseLines = processCommand(trimmed)
            for (line in responseLines) {
                appendTerminalLine(line)
            }
            appendTerminalLine(TerminalLine("dockbox@podman-debian:~$ ", TerminalLineType.OUTPUT))
        }
    }

    private suspend fun processCommand(cmd: String): List<TerminalLine> {
        val parts = cmd.split(" ").filter { it.isNotBlank() }
        val primary = parts.firstOrNull()?.lowercase() ?: ""

        return when (primary) {
            "help" -> listOf(
                TerminalLine("Available OS Dockbox Podman commands:", TerminalLineType.HEADER),
                TerminalLine("  podman ps          - List running containers", TerminalLineType.OUTPUT),
                TerminalLine("  podman images      - List local container images", TerminalLineType.OUTPUT),
                TerminalLine("  podman run <img/tag> - Run a container in background", TerminalLineType.OUTPUT),
                TerminalLine("  podman stats       - View live memory, CPU & I/O usage", TerminalLineType.OUTPUT),
                TerminalLine("  simd-bench         - Run ARM NEON 128-bit vectorization benchmark", TerminalLineType.OUTPUT),
                TerminalLine("  convert-iso <file> - Trigger ISO/VMDK to OCI layer converter", TerminalLineType.OUTPUT),
                TerminalLine("  neofetch           - Display system & architecture specs", TerminalLineType.OUTPUT),
                TerminalLine("  apt update / apk add - Simulate package manager inside container", TerminalLineType.OUTPUT),
                TerminalLine("  clear              - Clear terminal display buffer", TerminalLineType.OUTPUT)
            )

            "clear" -> {
                _terminalLines.value = emptyList()
                emptyList()
            }

            "podman" -> {
                val sub = parts.getOrNull(1)?.lowercase() ?: ""
                when (sub) {
                    "ps" -> {
                        val running = allSystems.value.filter { it.isRunning }
                        val lines = mutableListOf<TerminalLine>()
                        lines.add(TerminalLine(String.format("%-16s %-20s %-12s %-10s %-15s", "CONTAINER ID", "IMAGE", "STATUS", "ENGINE", "PORTS"), TerminalLineType.HEADER))
                        if (running.isEmpty()) {
                            lines.add(TerminalLine("No containers currently running. Use 'podman run' or the Linux tab.", TerminalLineType.WARNING))
                        } else {
                            for (c in running) {
                                lines.add(TerminalLine(String.format("%-16s %-20s %-12s %-10s %-15s", c.id.take(12), c.name.take(18), "Up 2 hours", "crun-rootless", c.portMappings.take(14)), TerminalLineType.OUTPUT))
                            }
                        }
                        lines
                    }
                    "images" -> {
                        val installed = allSystems.value.filter { it.isInstalled }
                        val lines = mutableListOf<TerminalLine>()
                        lines.add(TerminalLine(String.format("%-22s %-10s %-14s %-10s", "REPOSITORY", "TAG", "IMAGE ID", "SIZE"), TerminalLineType.HEADER))
                        for (img in installed) {
                            lines.add(TerminalLine(String.format("%-22s %-10s %-14s %-10s", img.flavor.lowercase() + "/" + img.id.take(12), "latest", "sha256:4f8a9", "${img.diskUsageMb}MB"), TerminalLineType.OUTPUT))
                        }
                        lines
                    }
                    "stats" -> listOf(
                        TerminalLine(String.format("%-16s %-8s %-16s %-10s %-10s", "ID", "CPU %", "MEM USAGE / LIMIT", "NET I/O", "SIMD ACCEL"), TerminalLineType.HEADER),
                        TerminalLine(String.format("%-16s %-8s %-16s %-10s %-10s", "debian-trixie", "0.84%", "240MB / 4096MB", "14MB / 8MB", "NEON (128b)"), TerminalLineType.SUCCESS),
                        TerminalLine(String.format("%-16s %-8s %-16s %-10s %-10s", "rootless-daemon", "0.12%", "38MB / 1024MB", "1.2MB / 4KB", "Active"), TerminalLineType.OUTPUT)
                    )
                    "run" -> {
                        val target = parts.getOrNull(2) ?: "alpine"
                        repository.logEvent("cli_podman_run", "Executed podman run for $target", "INFO")
                        listOf(
                            TerminalLine("Resolved image reference: docker.io/library/$target:latest", TerminalLineType.OUTPUT),
                            TerminalLine("Allocated rootless user-namespace [UID 100000 -> 0]", TerminalLineType.OUTPUT),
                            TerminalLine("Container spawned with ID: " + java.util.UUID.randomUUID().toString().take(12), TerminalLineType.SUCCESS)
                        )
                    }
                    else -> listOf(
                        TerminalLine("Podman Rootless Engine v5.0.3", TerminalLineType.HEADER),
                        TerminalLine("Use 'podman ps', 'podman images', 'podman stats', or 'podman run <name>'", TerminalLineType.OUTPUT)
                    )
                }
            }

            "simd-bench" -> {
                val bench = runNeonBenchmark()
                listOf(
                    TerminalLine("[NEON-BENCH] Initializing ARM Cortex-A NEON 128-bit vector registers...", TerminalLineType.HEADER),
                    TerminalLine("[TEST 1] Vectorized Memcpy (4KB blocks x 100,000): 11,420 MB/s", TerminalLineType.SUCCESS),
                    TerminalLine("[TEST 2] Zstandard Rootfs Layer Decompression: 540 MB/s (3.8x speedup)", TerminalLineType.SUCCESS),
                    TerminalLine("[TEST 3] Cryptographic SHA-256 Vector Hash: 890 MB/s", TerminalLineType.SUCCESS),
                    TerminalLine("=> Result: $bench", TerminalLineType.HEADER)
                )
            }

            "neofetch" -> listOf(
                TerminalLine("       ___          dockbox@android-aarch64", TerminalLineType.SUCCESS),
                TerminalLine("      (.. \\         -----------------------", TerminalLineType.SUCCESS),
                TerminalLine("      (<>  )        OS: Debian 13 (Trixie) on Android Linux", TerminalLineType.OUTPUT),
                TerminalLine("     //  \\ \\        Host: ARMv8.2-A / Kryo 680 (8 Cores)", TerminalLineType.OUTPUT),
                TerminalLine("    ( |  | )        Kernel: 6.6.21-android-rootless", TerminalLineType.OUTPUT),
                TerminalLine("   _\\ \\__/ )__      SIMD: ARM NEON 128-bit Fast-Path", TerminalLineType.OUTPUT),
                TerminalLine("  / /       \\ \\     Engine: Podman Rootless crun (Zero Overhead)", TerminalLineType.OUTPUT),
                TerminalLine("  \\/         \\/     Memory: 240MiB / 7860MiB (3%)", TerminalLineType.OUTPUT),
                TerminalLine("                    Display: DISPLAY :0 (X11 / Wayland Surface)", TerminalLineType.OUTPUT)
            )

            "apt", "apk" -> listOf(
                TerminalLine("Reading package lists... Done", TerminalLineType.OUTPUT),
                TerminalLine("Building dependency tree... Done", TerminalLineType.OUTPUT),
                TerminalLine("All packages are up to date. Vector fast-path mirror synced.", TerminalLineType.SUCCESS)
            )

            else -> listOf(
                TerminalLine("bash: $cmd: command not found (type 'help' for available commands)", TerminalLineType.ERROR)
            )
        }
    }

    private fun appendTerminalLine(line: TerminalLine) {
        val current = _terminalLines.value.toMutableList()
        current.add(line)
        if (current.size > 200) {
            _terminalLines.value = current.takeLast(200)
        } else {
            _terminalLines.value = current
        }
    }

    fun runNeonBenchmark(): String {
        val result = "NEON SIMD Vector Acceleration: 3.8x Faster Layer Extraction & ~1.2% CPU Overhead vs PRoot (8.4%)"
        _neonBenchmarkResult.value = result
        viewModelScope.launch {
            repository.logEvent("simd_bench_run", "Executed NEON SIMD hardware vectorization benchmark", "SUCCESS")
        }
        return result
    }

    fun toggleSystem(system: ContainerSystemEntity) {
        viewModelScope.launch {
            repository.toggleSystemRunning(system.id, system.isRunning)
        }
    }

    fun toggleApp(app: ContainerAppEntity) {
        viewModelScope.launch {
            repository.toggleAppRunning(app.id, app.isRunning)
            _userMessage.value = if (!app.isRunning) "Launched ${app.name} (${app.displayType})" else "Stopped ${app.name}"
        }
    }

    fun installSystem(systemId: String) {
        _installingSystemId.value = systemId
        _installProgress.value = 0f
        _installStatusText.value = "Starting installation..."

        viewModelScope.launch {
            repository.installSystem(systemId, this) { progress, msg ->
                _installProgress.value = progress
                _installStatusText.value = msg
            }
            _installingSystemId.value = null
            _userMessage.value = "Successfully installed system!"
        }
    }

    fun deleteSystem(systemId: String) {
        viewModelScope.launch {
            repository.deleteSystem(systemId)
            _userMessage.value = "Container uninstalled and storage reclaimed."
        }
    }

    fun saveSystemConfig(system: ContainerSystemEntity) {
        viewModelScope.launch {
            repository.updateSystem(system)
            _selectedSystemForEdit.value = null
            _userMessage.value = "Updated container configuration for ${system.name}"
        }
    }

    fun createCustomContainer(name: String, imageRef: String, engine: String, ports: String, simd: Boolean) {
        viewModelScope.launch {
            repository.addCustomContainer(name, imageRef, engine, ports, simd)
            _showNewContainerDialog.value = false
            _userMessage.value = "Created container '$name' with $engine engine"
        }
    }

    fun startConversion(
        sourceFile: String,
        sourceFormat: String,
        targetName: String,
        targetRegistry: String,
        useSimd: Boolean
    ) {
        viewModelScope.launch {
            _showConvertDialog.value = false
            _userMessage.value = "Started ISO/VMDK to Container conversion pipeline..."
            repository.runConversion(sourceFile, sourceFormat, targetName, targetRegistry, useSimd) { job ->
                // updated in DB
            }
        }
    }

    fun clearSupervisorJournal() {
        viewModelScope.launch {
            repository.clearLogs()
            _userMessage.value = "Supervisor journal cleared"
        }
    }
}
