package com.example.domain.model

enum class EngineType(val title: String, val performanceTax: String, val isolation: String) {
    PODMAN_ROOTLESS("Podman Rootless", "~1-2% CPU (Native Kernel)", "User Namespaces + crun"),
    PROOT_DISTRO("PRoot-Distro", "~6-10% CPU (Ptrace Syscalls)", "Userland Syscall Emulation"),
    QEMU_MICROVM("QEMU MicroVM", "~18-25% CPU (TCG JIT)", "Hypervisor / Full Virt")
}

data class SystemSpec(
    val deviceModel: String = "Pixel / ARM64 Workstation",
    val deviceManufacturer: String = "Google / ARM Holdings",
    val androidVersion: String = "Android 15 (API 35)",
    val kernelVersion: String = "Linux 6.6.21-android15-aarch64",
    val abi: String = "arm64-v8a (64-bit Little-Endian)",
    val cpuCores: Int = 8,
    val cpuFrequency: String = "2.84 GHz (Octa-Core)",
    val cpuUsagePercent: Float = 4.2f,
    val ramTotalMb: Long = 8192,
    val ramUsedMb: Long = 3480,
    val gpuRenderer: String = "Adreno (TM) 740 / Immortalis-G715",
    val gpuUsagePercent: Float = 8.5f,
    val vulkanVersion: String = "Vulkan 1.3.275",
    val openGlVersion: String = "OpenGL ES 3.2 v@530.0",
    val architecture: String = "aarch64 (ARMv8.2-A Kryo)",
    val neonSimdStatus: String = "ARM NEON 128-bit SIMD Active",
    val subuidRange: String = "100000:65536",
    val binfmtMisc: Boolean = true,
    val seccompBpf: Boolean = true,
    val rootlessDriver: String = "fuse-overlayfs (vfs fast-path)",
    val memoryTotalGb: Double = 8.0,
    val memoryFreeGb: Double = 4.6
)

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT,
    val id: String = java.util.UUID.randomUUID().toString()
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

data class DiskImageItem(
    val id: String,
    val fileName: String,
    val format: String,       // QCOW2, VHD, VMDK, RAW/IMG
    val partitionType: String,// EXT4, NTFS, BTRFS, FAT32, XFS
    val sizeGb: Int,
    val blockSize: String,    // 512B, 4KB, 64KB, 1MB
    val path: String,
    val status: String = "Ready",
    val createdAt: Long = System.currentTimeMillis()
)

data class DockerPushItem(
    val id: String,
    val imageName: String,
    val registry: String,
    val repository: String,
    val tag: String,
    val status: String,
    val progress: Float = 1f,
    val timestamp: Long = System.currentTimeMillis()
)

