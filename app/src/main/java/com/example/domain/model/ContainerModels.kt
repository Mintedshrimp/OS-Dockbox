package com.example.domain.model

enum class EngineType(val title: String, val performanceTax: String, val isolation: String) {
    PODMAN_ROOTLESS("Podman Rootless", "~1-2% CPU (Native Kernel)", "User Namespaces + crun"),
    PROOT_DISTRO("PRoot-Distro", "~6-10% CPU (Ptrace Syscalls)", "Userland Syscall Emulation"),
    QEMU_MICROVM("QEMU MicroVM", "~18-25% CPU (TCG JIT)", "Hypervisor / Full Virt")
}

data class SystemSpec(
    val architecture: String = "aarch64 (ARMv8.2-A)",
    val neonSimdStatus: String = "Hardware NEON Active (128-bit Vector)",
    val subuidRange: String = "100000:65536",
    val binfmtMisc: Boolean = true,
    val seccompBpf: Boolean = true,
    val rootlessDriver: String = "overlayfs (vfs fallback)",
    val memoryTotalGb: Double = 8.0,
    val memoryFreeGb: Double = 4.8
)

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT
)

enum class TerminalLineType {
    INPUT,
    OUTPUT,
    SUCCESS,
    ERROR,
    WARNING,
    HEADER
}

data class DistroTemplate(
    val id: String,
    val name: String,
    val version: String,
    val flavor: String,
    val desktopEnv: String,
    val isRecommended: Boolean = false,
    val isOfficial: Boolean = true,
    val defaultEngine: String = "Podman Rootless",
    val defaultSizeMb: Long = 480
)
