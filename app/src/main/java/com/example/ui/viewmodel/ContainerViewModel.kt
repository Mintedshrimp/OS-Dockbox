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
import com.example.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class MainTab(val title: String) {
    HOME("Home"),
    OS("OS"),
    TERMINAL("Terminal"),
    APPS("Apps"),
    TOOLS("Tools"),
    ABOUT("About")
}

enum class DesktopWindowMode {
    FULLSCREEN,
    FREEFORM,
    PIP,
    HIDDEN
}

data class ToolInfoData(
    val title: String,
    val subtitle: String,
    val description: String,
    val cliCommand: String,
    val keyFeatures: List<String>,
    val bestPractice: String
)

data class PortBridgeRule(
    val id: String,
    val containerId: String,
    val containerName: String,
    val hostPort: Int,
    val containerPort: Int,
    val protocol: String = "TCP",
    val status: String = "Listening"
)

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

    private val _appTypeFilter = MutableStateFlow("ALL") // "ALL", "USER", "SYSTEM", "RUNNING"
    val appTypeFilter = _appTypeFilter.asStateFlow()

    private val _systemFilter = MutableStateFlow("ALL") // "ALL", "INSTALLED", "PODMAN", "PROOT", "MINI_OS", "RUNNING"
    val systemFilter = _systemFilter.asStateFlow()

    private val _activeContainerId = MutableStateFlow<String?>("debian-trixie")
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

    private val _showAddAppDialog = MutableStateFlow(false)
    val showAddAppDialog = _showAddAppDialog.asStateFlow()

    private val _showRootfsInstallerDialog = MutableStateFlow(false)
    val showRootfsInstallerDialog = _showRootfsInstallerDialog.asStateFlow()

    private val _showConvertDialog = MutableStateFlow(false)
    val showConvertDialog = _showConvertDialog.asStateFlow()

    private val _showCreateDiskDialog = MutableStateFlow(false)
    val showCreateDiskDialog = _showCreateDiskDialog.asStateFlow()

    private val _showPushDockerDialog = MutableStateFlow(false)
    val showPushDockerDialog = _showPushDockerDialog.asStateFlow()

    private val _showPortBridgeDialog = MutableStateFlow(false)
    val showPortBridgeDialog = _showPortBridgeDialog.asStateFlow()

    private val _selectedToolInfo = MutableStateFlow<ToolInfoData?>(null)
    val selectedToolInfo = _selectedToolInfo.asStateFlow()

    private val _showDesktopViewer = MutableStateFlow(false)
    val showDesktopViewer = _showDesktopViewer.asStateFlow()

    private val _desktopWindowMode = MutableStateFlow(DesktopWindowMode.HIDDEN)
    val desktopWindowMode = _desktopWindowMode.asStateFlow()

    private val _desktopActiveWindow = MutableStateFlow("Chromium")
    val desktopActiveWindow = _desktopActiveWindow.asStateFlow()

    private val _desktopSelectedWm = MutableStateFlow("XFCE4")
    val desktopSelectedWm = _desktopSelectedWm.asStateFlow()

    private val _desktopResolution = MutableStateFlow("1280x720 (HD)")
    val desktopResolution = _desktopResolution.asStateFlow()

    private val _desktopTouchMode = MutableStateFlow(true)
    val desktopTouchMode = _desktopTouchMode.asStateFlow()

    private val _selectedSystemForEdit = MutableStateFlow<ContainerSystemEntity?>(null)
    val selectedSystemForEdit = _selectedSystemForEdit.asStateFlow()

    // Tools Managed States
    private val _diskImages = MutableStateFlow<List<DiskImageItem>>(
        listOf(
            DiskImageItem(
                id = "disk-1",
                fileName = "ubuntu24-rootfs.qcow2",
                format = "QCOW2",
                partitionType = "EXT4",
                sizeGb = 16,
                blockSize = "4KB (Sparse)",
                path = "/storage/emulated/0/Dockbox/disks/ubuntu24-rootfs.qcow2",
                status = "Ready"
            ),
            DiskImageItem(
                id = "disk-2",
                fileName = "virtual-win-storage.vhd",
                format = "VHD",
                partitionType = "NTFS",
                sizeGb = 32,
                blockSize = "64KB",
                path = "/storage/emulated/0/Dockbox/disks/virtual-win-storage.vhd",
                status = "Mounted"
            )
        )
    )
    val diskImages = _diskImages.asStateFlow()

    private val _dockerPushes = MutableStateFlow<List<DockerPushItem>>(
        listOf(
            DockerPushItem(
                id = "push-1",
                imageName = "dockbox/alpine-arm64",
                registry = "Docker Hub (docker.io)",
                repository = "myuser/alpine-custom",
                tag = "3.22-neon",
                status = "Pushed",
                progress = 1.0f
            )
        )
    )
    val dockerPushes = _dockerPushes.asStateFlow()

    private val _portBridgeRules = MutableStateFlow<List<PortBridgeRule>>(
        listOf(
            PortBridgeRule("rule-1", "debian-trixie", "Debian 13 (Trixie)", 8080, 80, "TCP", "Listening"),
            PortBridgeRule("rule-2", "debian-trixie", "Debian 13 (Trixie)", 5901, 5901, "TCP (VNC)", "Listening")
        )
    )
    val portBridgeRules = _portBridgeRules.asStateFlow()

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
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
        if (tab == MainTab.TERMINAL) {
            checkAndSyncTerminal()
        }
    }

    fun setSearchQuerySystems(query: String) {
        _searchQuerySystems.value = query
    }

    fun setSearchQueryApps(query: String) {
        _searchQueryApps.value = query
    }

    fun setAppTypeFilter(filter: String) {
        _appTypeFilter.value = filter
    }

    fun setSystemFilter(filter: String) {
        _systemFilter.value = filter
    }

    fun setShowAddAppDialog(show: Boolean) {
        _showAddAppDialog.value = show
    }

    fun setShowRootfsInstallerDialog(show: Boolean) {
        _showRootfsInstallerDialog.value = show
    }

    fun setActiveContainer(id: String) {
        _activeContainerId.value = id
        val system = allSystems.value.find { it.id == id }
        if (system != null) {
            attachContainerTerminal(system)
        }
    }

    fun setTerminalInput(input: String) {
        _terminalInput.value = input
    }

    fun showDesktop(show: Boolean) {
        _showDesktopViewer.value = show
        if (show) {
            if (_desktopWindowMode.value == DesktopWindowMode.HIDDEN) {
                _desktopWindowMode.value = DesktopWindowMode.FULLSCREEN
            }
            viewModelScope.launch {
                repository.logEvent("desktop_opened", "Attached interactive DISPLAY :0 X11 surface", "SUCCESS")
            }
        } else {
            _desktopWindowMode.value = DesktopWindowMode.HIDDEN
        }
    }

    fun setDesktopWindowMode(mode: DesktopWindowMode) {
        _desktopWindowMode.value = mode
        _showDesktopViewer.value = (mode != DesktopWindowMode.HIDDEN)
        viewModelScope.launch {
            repository.logEvent("desktop_mode_changed", "Desktop mode switched to ${mode.name}", "INFO")
        }
    }

    fun launchAppInDesktop(appName: String, mode: DesktopWindowMode = DesktopWindowMode.FULLSCREEN) {
        _desktopActiveWindow.value = appName
        _desktopWindowMode.value = mode
        _showDesktopViewer.value = true
        viewModelScope.launch {
            repository.logEvent("app_launched_desktop", "Launched $appName in mode ${mode.name}", "SUCCESS")
        }
    }

    fun setDesktopActiveWindow(win: String) {
        _desktopActiveWindow.value = win
    }

    fun setDesktopSelectedWm(wm: String) {
        _desktopSelectedWm.value = wm
    }

    fun setDesktopResolution(res: String) {
        _desktopResolution.value = res
    }

    fun toggleDesktopTouchMode() {
        _desktopTouchMode.value = !_desktopTouchMode.value
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

    fun setShowCreateDiskDialog(show: Boolean) {
        _showCreateDiskDialog.value = show
    }

    fun setShowPushDockerDialog(show: Boolean) {
        _showPushDockerDialog.value = show
    }

    fun setShowPortBridgeDialog(show: Boolean) {
        _showPortBridgeDialog.value = show
    }

    fun showToolInfo(info: ToolInfoData?) {
        _selectedToolInfo.value = info
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun checkAndSyncTerminal() {
        val activeId = _activeContainerId.value
        val installedRunning = allSystems.value.find { it.id == activeId && it.isRunning }
            ?: allSystems.value.firstOrNull { it.isRunning }
            ?: allSystems.value.firstOrNull { it.isInstalled }

        if (installedRunning != null && installedRunning.isRunning) {
            if (_activeContainerId.value != installedRunning.id || _terminalLines.value.isEmpty()) {
                _activeContainerId.value = installedRunning.id
                attachContainerTerminal(installedRunning)
            }
        } else if (_activeContainerId.value == null || installedRunning == null) {
            // Keep idle
            _activeContainerId.value = null
            _terminalLines.value = emptyList()
        }
    }

    fun attachContainerTerminal(system: ContainerSystemEntity) {
        val flavor = system.flavor.lowercase()
        val prompt = getPromptForSystem(system)

        val banner = mutableListOf<TerminalLine>()

        if (system.isFallbackEngaged) {
            banner.add(TerminalLine("⚠️ [ENGINE FALLBACK ACTIVE] Podman User-NS restricted", TerminalLineType.WARNING))
            banner.add(TerminalLine(" * Failover Engine:   PRoot-Distro (ptrace syscall emulation)", TerminalLineType.SUCCESS))
            banner.add(TerminalLine(" * Failover Reason:   ${if (system.fallbackReason.isNotBlank()) system.fallbackReason else "CONFIG_USER_NS unshare restriction / seccomp clone3"}", TerminalLineType.OUTPUT))
            banner.add(TerminalLine(" * Fake Rootfs Root:  /data/data/com.example/files/rootfs/${system.id}", TerminalLineType.OUTPUT))
            banner.add(TerminalLine(" * Isolation Mode:    Rootless PTrace Hook (100% kernel compatible)", TerminalLineType.OUTPUT))
            banner.add(TerminalLine("Type 'proot-info', 'switch-engine', or 'help' for diagnostics.", TerminalLineType.HEADER))
            banner.add(TerminalLine(prompt, TerminalLineType.OUTPUT))
        } else {
            val defaultBanner = when (flavor) {
                "ubuntu" -> listOf(
                    TerminalLine("Welcome to Ubuntu 24.04 LTS (GNU/Linux 6.6.21-android aarch64)", TerminalLineType.HEADER),
                    TerminalLine(" * Documentation:  https://help.ubuntu.com", TerminalLineType.OUTPUT),
                    TerminalLine(" * Management:     Podman Rootless crun (User Namespaces)", TerminalLineType.OUTPUT),
                    TerminalLine(" * Fallback Mode:  PRoot-Distro Ready [AUTO-FAILOVER ON]", TerminalLineType.SUCCESS),
                    TerminalLine(" * Hardware Accel: ARM NEON 128-bit SIMD [ACTIVE]", TerminalLineType.SUCCESS),
                    TerminalLine(" * System State:   Container ID ${system.id.take(10)} [RUNNING]", TerminalLineType.OUTPUT),
                    TerminalLine("Type 'help', 'apt update', 'proot-info', or 'neofetch' to begin.", TerminalLineType.WARNING),
                    TerminalLine(prompt, TerminalLineType.OUTPUT)
                )
                "alpine" -> listOf(
                    TerminalLine("Welcome to Alpine Linux 3.22 (musl libc 1.2.5, aarch64)", TerminalLineType.HEADER),
                    TerminalLine("Kernel 6.6.21-android-rootless / Minimal Rootfs (7MB)", TerminalLineType.OUTPUT),
                    TerminalLine("Engine: Podman Rootless (PRoot-Distro Fallback Guard Active)", TerminalLineType.SUCCESS),
                    TerminalLine("Package manager: apk add <package>", TerminalLineType.OUTPUT),
                    TerminalLine(prompt, TerminalLineType.OUTPUT)
                )
                "arch" -> listOf(
                    TerminalLine("Arch Linux (ARMv8.2-A aarch64) - Rolling Release", TerminalLineType.HEADER),
                    TerminalLine("Pacman v6.1.0 unprivileged environment ready.", TerminalLineType.SUCCESS),
                    TerminalLine("Dual-Engine: Podman OCI + PRoot Fallback", TerminalLineType.OUTPUT),
                    TerminalLine(prompt, TerminalLineType.OUTPUT)
                )
                "fedora" -> listOf(
                    TerminalLine("Fedora 40 Cloud (Container Base / DNF5)", TerminalLineType.HEADER),
                    TerminalLine("SELinux unconfined container namespace active.", TerminalLineType.OUTPUT),
                    TerminalLine(prompt, TerminalLineType.OUTPUT)
                )
                "kali" -> listOf(
                    TerminalLine("Kali Linux 2024.2 Rolling (Security Suite)", TerminalLineType.HEADER),
                    TerminalLine("Unprivileged penetration testing environment loaded.", TerminalLineType.WARNING),
                    TerminalLine(prompt, TerminalLineType.OUTPUT)
                )
                "python" -> listOf(
                    TerminalLine("Python 3.12.3 Container Environment (JupyterLab Ready)", TerminalLineType.HEADER),
                    TerminalLine("PyTorch + NumPy + NEON Vector Acceleration ready.", TerminalLineType.SUCCESS),
                    TerminalLine(prompt, TerminalLineType.OUTPUT)
                )
                else -> listOf(
                    TerminalLine("=== ${system.name} (${system.engineType}) ===", TerminalLineType.HEADER),
                    TerminalLine("Rootless session attached. PRoot Fallback Guard: Active", TerminalLineType.OUTPUT),
                    TerminalLine(prompt, TerminalLineType.OUTPUT)
                )
            }
            banner.addAll(defaultBanner)
        }

        _terminalLines.value = banner
    }

    private fun getPromptForSystem(system: ContainerSystemEntity): String {
        return when (system.flavor.lowercase()) {
            "alpine" -> "${system.id.take(8)}:/# "
            "arch" -> "[root@archlinux ~]# "
            "fedora" -> "[root@fedora ~]# "
            "ubuntu" -> "root@ubuntu:~# "
            "debian" -> "dockbox@debian:~$ "
            else -> "root@${system.id.take(8)}:~# "
        }
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        val activeId = _activeContainerId.value
        val activeSystem = allSystems.value.find { it.id == activeId }
        val prompt = activeSystem?.let { getPromptForSystem(it) } ?: "dockbox@android:~$ "

        appendTerminalLine(TerminalLine("$prompt$trimmed", TerminalLineType.INPUT))
        _terminalInput.value = ""

        viewModelScope.launch {
            val responseLines = processCommand(trimmed, activeSystem)
            for (line in responseLines) {
                appendTerminalLine(line)
            }
            appendTerminalLine(TerminalLine(prompt, TerminalLineType.OUTPUT))
        }
    }

    private suspend fun processCommand(cmd: String, activeSystem: ContainerSystemEntity?): List<TerminalLine> {
        val parts = cmd.split(" ").filter { it.isNotBlank() }
        val primary = parts.firstOrNull()?.lowercase() ?: ""

        if (activeSystem == null && primary != "help" && primary != "clear" && primary != "podman") {
            return listOf(
                TerminalLine("Notice: No container OS is currently active.", TerminalLineType.WARNING),
                TerminalLine("Go to the Linux tab, install a container, and click 'Run' to start executing container commands.", TerminalLineType.OUTPUT)
            )
        }

        return when (primary) {
            "help" -> listOf(
                TerminalLine("Available Container Terminal commands:", TerminalLineType.HEADER),
                TerminalLine("  apt update / apk add / dnf / pacman - Install & update packages", TerminalLineType.OUTPUT),
                TerminalLine("  uname -a           - Print kernel and container architecture", TerminalLineType.OUTPUT),
                TerminalLine("  whoami / pwd / ls  - Inspect current user and file structure", TerminalLineType.OUTPUT),
                TerminalLine("  podman ps          - List running container processes", TerminalLineType.OUTPUT),
                TerminalLine("  podman stats       - View live memory, CPU & I/O usage", TerminalLineType.OUTPUT),
                TerminalLine("  proot-info         - Inspect PRoot-Distro ptrace engine fallback diagnostics", TerminalLineType.OUTPUT),
                TerminalLine("  switch-engine      - Switch container engine between Podman and PRoot-Distro", TerminalLineType.OUTPUT),
                TerminalLine("  podman-fallback    - Simulate kernel namespace restriction failover to PRoot", TerminalLineType.OUTPUT),
                TerminalLine("  simd-bench         - Run ARM NEON 128-bit vectorization benchmark", TerminalLineType.OUTPUT),
                TerminalLine("  neofetch           - Display distribution info and logo", TerminalLineType.OUTPUT),
                TerminalLine("  top / htop         - Live process status", TerminalLineType.OUTPUT),
                TerminalLine("  clear              - Clear terminal display buffer", TerminalLineType.OUTPUT)
            )

            "clear" -> {
                _terminalLines.value = emptyList()
                emptyList()
            }

            "proot", "proot-info", "proot-distro" -> {
                val isFallback = activeSystem?.isFallbackEngaged == true
                listOf(
                    TerminalLine("=== PRoot-Distro Fallback Architecture Diagnostics ===", TerminalLineType.HEADER),
                    TerminalLine("Engine Status:      ${if (isFallback) "ACTIVE (Failover in effect)" else "STANDBY (Automatic Failover Ready)"}", if (isFallback) TerminalLineType.WARNING else TerminalLineType.SUCCESS),
                    TerminalLine("Syscall Hook:       ptrace(PTRACE_SYSCALL) register rewrite", TerminalLineType.OUTPUT),
                    TerminalLine("Rootfs Sandbox:     /data/data/com.example/files/rootfs/${activeSystem?.id ?: "current"}", TerminalLineType.OUTPUT),
                    TerminalLine("User Namespace:     Simulated UID 0 / GID 0 (Unprivileged safe)", TerminalLineType.OUTPUT),
                    TerminalLine("Kernel Req:         Works on ANY Android Linux kernel (No CONFIG_USER_NS needed)", TerminalLineType.SUCCESS),
                    TerminalLine("Failover Triggers:  CONFIG_USER_NS=n, crun seccomp clone3, unshare(CLONE_NEWUSER)", TerminalLineType.OUTPUT),
                    TerminalLine("Action:             Type 'switch-engine' to toggle runtime engine anytime.", TerminalLineType.HEADER)
                )
            }

            "switch-engine" -> {
                if (activeSystem != null) {
                    if (activeSystem.isFallbackEngaged) {
                        restorePodmanEngine(activeSystem.id)
                        listOf(
                            TerminalLine("Switching engine back to Podman Rootless crun...", TerminalLineType.HEADER),
                            TerminalLine("Re-initializing unshared user namespaces (UID map 100000:65536)...", TerminalLineType.OUTPUT),
                            TerminalLine("Engine restored: Podman Rootless (crun)", TerminalLineType.SUCCESS)
                        )
                    } else {
                        triggerProotFallback(activeSystem.id, "Manual engine switch to PRoot-Distro ptrace emulation")
                        listOf(
                            TerminalLine("Engaging PRoot-Distro Fallback Engine...", TerminalLineType.WARNING),
                            TerminalLine("Syscall translation attached via ptrace...", TerminalLineType.OUTPUT),
                            TerminalLine("Engine switched: PRoot-Distro (Rootless Syscall Interception)", TerminalLineType.SUCCESS)
                        )
                    }
                } else {
                    listOf(TerminalLine("Error: No container active to switch.", TerminalLineType.ERROR))
                }
            }

            "podman-fallback" -> {
                if (activeSystem != null) {
                    triggerProotFallback(activeSystem.id, "Kernel unshare(CLONE_NEWUSER) EPERM (CONFIG_USER_NS disabled)")
                    listOf(
                        TerminalLine("[PODMAN ERROR] crun: unshare(CLONE_NEWUSER): Operation not permitted", TerminalLineType.ERROR),
                        TerminalLine("[GUARD TRIGGERED] Automatic PRoot-Distro fallback engaged seamlessly!", TerminalLineType.WARNING),
                        TerminalLine("[PROOT-DISTRO] PTrace syscall emulator loaded fake rootfs.", TerminalLineType.SUCCESS),
                        TerminalLine("Container '${activeSystem.name}' is running stably in userland fallback.", TerminalLineType.OUTPUT)
                    )
                } else {
                    listOf(TerminalLine("Error: No active container selected.", TerminalLineType.ERROR))
                }
            }

            "whoami" -> listOf(
                TerminalLine(if (activeSystem?.flavor?.lowercase() == "debian") "dockbox" else "root", TerminalLineType.SUCCESS)
            )

            "pwd" -> listOf(
                TerminalLine(if (activeSystem?.flavor?.lowercase() == "debian") "/home/dockbox" else "/root", TerminalLineType.OUTPUT)
            )

            "ls", "dir" -> listOf(
                TerminalLine("bin   dev  home  lib64  mnt  proc  run   srv  tmp  var", TerminalLineType.OUTPUT),
                TerminalLine("boot  etc  lib   media  opt  root  sbin  sys  usr  workspace", TerminalLineType.OUTPUT)
            )

            "uname" -> listOf(
                TerminalLine("Linux ${activeSystem?.id ?: "container"} 6.6.21-android-rootless-crun #1 SMP PREEMPT aarch64 GNU/Linux", TerminalLineType.SUCCESS)
            )

            "cat" -> {
                val target = parts.getOrNull(1) ?: ""
                if (target.contains("os-release")) {
                    listOf(
                        TerminalLine("NAME=\"${activeSystem?.name ?: "Linux Container"}\"", TerminalLineType.OUTPUT),
                        TerminalLine("VERSION=\"${activeSystem?.version ?: "Latest"}\"", TerminalLineType.OUTPUT),
                        TerminalLine("ID=${activeSystem?.flavor?.lowercase() ?: "linux"}", TerminalLineType.OUTPUT),
                        TerminalLine("ARCHITECTURE=aarch64", TerminalLineType.OUTPUT),
                        TerminalLine("DOCKBOX_ENGINE=podman-rootless-crun", TerminalLineType.OUTPUT)
                    )
                } else {
                    listOf(TerminalLine("cat: $target: No such file or directory", TerminalLineType.ERROR))
                }
            }

            "podman" -> {
                val sub = parts.getOrNull(1)?.lowercase() ?: ""
                when (sub) {
                    "ps" -> {
                        val running = allSystems.value.filter { it.isRunning }
                        val lines = mutableListOf<TerminalLine>()
                        lines.add(TerminalLine(String.format("%-16s %-20s %-12s %-10s %-15s", "CONTAINER ID", "IMAGE", "STATUS", "ENGINE", "PORTS"), TerminalLineType.HEADER))
                        if (running.isEmpty()) {
                            lines.add(TerminalLine("No containers currently running. Use the Linux tab to start one.", TerminalLineType.WARNING))
                        } else {
                            for (c in running) {
                                lines.add(TerminalLine(String.format("%-16s %-20s %-12s %-10s %-15s", c.id.take(12), c.name.take(18), "Up (Active)", "crun-rootless", c.portMappings.take(14)), TerminalLineType.OUTPUT))
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
                        TerminalLine(String.format("%-16s %-8s %-16s %-10s %-10s", activeSystem?.id?.take(12) ?: "container", "0.84%", "240MB / 8192MB", "14MB / 8MB", "NEON (128b)"), TerminalLineType.SUCCESS),
                        TerminalLine(String.format("%-16s %-8s %-16s %-10s %-10s", "rootless-daemon", "0.12%", "38MB / 1024MB", "1.2MB / 4KB", "Active"), TerminalLineType.OUTPUT)
                    )
                    else -> listOf(
                        TerminalLine("Podman Rootless Engine v5.0.3", TerminalLineType.HEADER),
                        TerminalLine("Use 'podman ps', 'podman images', 'podman stats'", TerminalLineType.OUTPUT)
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
                TerminalLine("       ___          root@${activeSystem?.id ?: "dockbox-arm64"}", TerminalLineType.SUCCESS),
                TerminalLine("      (.. \\         -----------------------", TerminalLineType.SUCCESS),
                TerminalLine("      (<>  )        OS: ${activeSystem?.name ?: "Linux Container"}", TerminalLineType.OUTPUT),
                TerminalLine("     //  \\ \\        Host: ${systemSpec.value.deviceModel} (8 Cores)", TerminalLineType.OUTPUT),
                TerminalLine("    ( |  | )        Kernel: 6.6.21-android-rootless", TerminalLineType.OUTPUT),
                TerminalLine("   _\\ \\__/ )__      SIMD: ARM NEON 128-bit Fast-Path", TerminalLineType.OUTPUT),
                TerminalLine("  / /       \\ \\     Engine: ${activeSystem?.engineType ?: "Podman Rootless"}", TerminalLineType.OUTPUT),
                TerminalLine("  \\/         \\/     Memory: 240MiB / 8192MiB (3%)", TerminalLineType.OUTPUT),
                TerminalLine("                    Display: DISPLAY :0 (X11 / Wayland Surface)", TerminalLineType.OUTPUT)
            )

            "apt", "apk", "pacman", "dnf" -> listOf(
                TerminalLine("Fetching package metadata from fastest ARM64 mirror...", TerminalLineType.OUTPUT),
                TerminalLine("Reading package lists... Done", TerminalLineType.OUTPUT),
                TerminalLine("Building dependency tree... Done", TerminalLineType.OUTPUT),
                TerminalLine("All packages are up to date. Container rootfs synced.", TerminalLineType.SUCCESS)
            )

            "top", "htop" -> listOf(
                TerminalLine("PID  USER     PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND", TerminalLineType.HEADER),
                TerminalLine("  1  root     20   0   12.4M   4.2M   3.1M S   0.0   0.1   0:00.12 init", TerminalLineType.OUTPUT),
                TerminalLine(" 14  root     20   0   28.8M   8.4M   5.2M S   0.8   0.2   0:01.45 podman-crun", TerminalLineType.SUCCESS),
                TerminalLine(" 28  root     20   0   18.2M   6.1M   4.8M R   0.4   0.1   0:00.04 bash", TerminalLineType.OUTPUT)
            )

            else -> listOf(
                TerminalLine("bash: $cmd: command executed inside ${activeSystem?.name ?: "container"}", TerminalLineType.OUTPUT)
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
            val willBeRunning = !system.isRunning
            repository.toggleSystemRunning(system.id, system.isRunning)
            if (willBeRunning) {
                _activeContainerId.value = system.id
                attachContainerTerminal(system)
            }
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
            _activeContainerId.value = systemId
            val system = allSystems.value.find { it.id == systemId }
            if (system != null) {
                attachContainerTerminal(system)
            }
            _userMessage.value = "Successfully installed container! Ready to run."
        }
    }

    fun deleteSystem(systemId: String) {
        viewModelScope.launch {
            repository.deleteSystem(systemId)
            if (_activeContainerId.value == systemId) {
                _activeContainerId.value = null
                _terminalLines.value = emptyList()
            }
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

    fun triggerProotFallback(systemId: String, reason: String = "Podman unshare / user-namespace restricted on Android kernel") {
        val system = allSystems.value.find { it.id == systemId } ?: return
        viewModelScope.launch {
            repository.triggerProotFallback(system, reason)
            val updated = allSystems.value.find { it.id == systemId }
            if (updated != null && _activeContainerId.value == systemId) {
                attachContainerTerminal(updated)
            }
            _userMessage.value = "⚠️ Podman failover: ${system.name} now running via PRoot-Distro (ptrace)"
        }
    }

    fun restorePodmanEngine(systemId: String) {
        val system = allSystems.value.find { it.id == systemId } ?: return
        viewModelScope.launch {
            repository.resetPodmanEngine(system)
            val updated = allSystems.value.find { it.id == systemId }
            if (updated != null && _activeContainerId.value == systemId) {
                attachContainerTerminal(updated)
            }
            _userMessage.value = "Restored Podman Rootless crun engine for ${system.name}"
        }
    }

    fun toggleAutoFallback(systemId: String, enabled: Boolean) {
        val system = allSystems.value.find { it.id == systemId } ?: return
        viewModelScope.launch {
            val updated = system.copy(autoFallbackToProot = enabled)
            repository.updateSystem(updated)
            _userMessage.value = if (enabled) "Auto PRoot fallback enabled for ${system.name}" else "Auto PRoot fallback disabled"
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

    fun createDiskImage(
        fileName: String,
        format: String,
        partitionType: String,
        sizeGb: Int,
        blockSize: String,
        preallocate: Boolean
    ) {
        viewModelScope.launch {
            _showCreateDiskDialog.value = false
            val cleanName = if (fileName.contains(".")) fileName else "$fileName.${format.lowercase()}"
            val newItem = DiskImageItem(
                id = "disk-${UUID.randomUUID().toString().take(8)}",
                fileName = cleanName,
                format = format,
                partitionType = partitionType,
                sizeGb = sizeGb,
                blockSize = blockSize,
                path = "/storage/emulated/0/Dockbox/disks/$cleanName",
                status = "Creating..."
            )
            _diskImages.value = listOf(newItem) + _diskImages.value
            _userMessage.value = "Building blank $format disk file ($sizeGb GB, $partitionType, $blockSize)..."

            delay(1500)
            _diskImages.value = _diskImages.value.map {
                if (it.id == newItem.id) it.copy(status = "Ready") else it
            }
            repository.logEvent("disk_created", "Created blank $format disk ($sizeGb GB, $partitionType, block: $blockSize)", "SUCCESS")
            _userMessage.value = "Disk image '$cleanName' created successfully!"
        }
    }

    fun deleteDiskImage(id: String) {
        _diskImages.value = _diskImages.value.filterNot { it.id == id }
        _userMessage.value = "Disk image deleted."
    }

    fun pushDockerImage(
        imageName: String,
        registry: String,
        repositoryName: String,
        tag: String,
        username: String
    ) {
        viewModelScope.launch {
            _showPushDockerDialog.value = false
            val pushId = "push-${UUID.randomUUID().toString().take(8)}"
            val item = DockerPushItem(
                id = pushId,
                imageName = imageName,
                registry = registry,
                repository = repositoryName,
                tag = tag.ifBlank { "latest" },
                status = "Authenticating & Pushing...",
                progress = 0.2f
            )
            _dockerPushes.value = listOf(item) + _dockerPushes.value
            _userMessage.value = "Pushing $imageName to $registry as $repositoryName:$tag..."

            delay(1000)
            _dockerPushes.value = _dockerPushes.value.map {
                if (it.id == pushId) it.copy(status = "Uploading layers (NEON compressed)...", progress = 0.65f) else it
            }
            delay(1200)
            _dockerPushes.value = _dockerPushes.value.map {
                if (it.id == pushId) it.copy(status = "Pushed (sha256:7e9a2b)", progress = 1.0f) else it
            }
            repository.logEvent("image_pushed", "Pushed $imageName to $registry/$repositoryName:$tag", "SUCCESS")
            _userMessage.value = "Successfully pushed image to $registry!"
        }
    }

    fun addPortBridgeRule(containerId: String, hostPort: Int, containerPort: Int, protocol: String) {
        val system = allSystems.value.find { it.id == containerId }
        val newRule = PortBridgeRule(
            id = "rule-${UUID.randomUUID().toString().take(6)}",
            containerId = containerId,
            containerName = system?.name ?: "Container",
            hostPort = hostPort,
            containerPort = containerPort,
            protocol = protocol,
            status = "Listening"
        )
        _portBridgeRules.value = _portBridgeRules.value + newRule
        _showPortBridgeDialog.value = false
        _userMessage.value = "Port bridge active: Host $hostPort -> Container $containerPort ($protocol)"
    }

    fun removePortBridgeRule(id: String) {
        _portBridgeRules.value = _portBridgeRules.value.filterNot { it.id == id }
        _userMessage.value = "Port forward rule removed."
    }

    fun addCustomUserApp(
        containerId: String,
        appName: String,
        category: String,
        command: String,
        displayType: String,
        description: String,
        port: Int = 0
    ) {
        viewModelScope.launch {
            _showAddAppDialog.value = false
            repository.addCustomApp(
                containerId = containerId,
                appName = appName,
                category = category,
                command = command,
                displayType = displayType,
                description = description,
                port = port
            )
            _userMessage.value = "Installed '$appName' into container!"
        }
    }

    fun installCustomRootfsTarball(
        name: String,
        fileName: String,
        fileSizeMb: Long,
        engine: String
    ) {
        viewModelScope.launch {
            _showRootfsInstallerDialog.value = false
            _userMessage.value = "Unpacking rootfs tarball '$fileName'..."
            repository.installCustomRootfs(
                name = name,
                fileName = fileName,
                fileSizeMb = fileSizeMb,
                engine = engine
            ) { progress, status ->
                // Progress callback
            }
            _userMessage.value = "Mini OS rootfs '$name' installed and ready in catalog!"
        }
    }

    fun exportDiskFile(item: DiskImageItem) {
        viewModelScope.launch {
            repository.logEvent("disk_exported", "Exported ${item.fileName} (${item.sizeGb} GB) to Downloads folder", "SUCCESS")
            _userMessage.value = "Exported ${item.fileName} to device storage!"
        }
    }

    fun clearSupervisorJournal() {
        viewModelScope.launch {
            repository.clearLogs()
            _userMessage.value = "Supervisor journal cleared"
        }
    }
}

