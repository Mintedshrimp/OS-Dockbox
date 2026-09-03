package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.DiskImageItem
import com.example.domain.model.DockerPushItem
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.PortBridgeRule
import com.example.ui.viewmodel.ToolInfoData

@Composable
fun ToolsScreen(
    viewModel: ContainerViewModel,
    modifier: Modifier = Modifier
) {
    val conversions by viewModel.conversions.collectAsStateWithLifecycle()
    val diskImages by viewModel.diskImages.collectAsStateWithLifecycle()
    val dockerPushes by viewModel.dockerPushes.collectAsStateWithLifecycle()
    val portRules by viewModel.portBridgeRules.collectAsStateWithLifecycle()
    val allSystems by viewModel.allSystems.collectAsStateWithLifecycle()
    val installedSystems = remember(allSystems) { allSystems.filter { it.isInstalled } }

    val showConvertDialog by viewModel.showConvertDialog.collectAsStateWithLifecycle()
    val showCreateDiskDialog by viewModel.showCreateDiskDialog.collectAsStateWithLifecycle()
    val showPushDockerDialog by viewModel.showPushDockerDialog.collectAsStateWithLifecycle()
    val showPortBridgeDialog by viewModel.showPortBridgeDialog.collectAsStateWithLifecycle()
    val selectedToolInfo by viewModel.selectedToolInfo.collectAsStateWithLifecycle()

    var selectedSection by remember { mutableStateOf("DISKS") } // DISKS, CONVERSIONS, PUSHES, PORTS

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UDroidBg),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            HeaderBar(
                title = "OS Dockbox",
                statusText = "ROOTLESS TOOLS",
                version = "v0.1.1"
            )
        }

        // Title & Description
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Text(
                    text = "Tools",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = UDroidTextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Convert OS images, create disk files, push containers, and manage networking",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UDroidTextSecondary
                )
            }
        }

        // Section: Primary Tools Grid / List
        item {
            Text(
                text = "UTILITIES SUITE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UDroidTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 6.dp)
            )
        }

        // Tool 1: ISO / VMDK / VHD to Container Converter
        item {
            ToolCardItem(
                icon = Icons.Default.Transform,
                iconBg = Color(0xFFE0F2FE),
                iconTint = Color(0xFF0284C7),
                title = "ISO / VMDK Converter",
                subtitle = "Extract rootfs from ISO, VMDK, and VHD images into OCI Podman layers",
                badge = "OCI Layer Packer",
                badgeColor = Color(0xFF0284C7),
                onInfoClick = {
                    viewModel.showToolInfo(
                        ToolInfoData(
                            title = "ISO & VMDK to OCI Converter",
                            subtitle = "Converts monolithic OS disk images into lightweight unprivileged container rootfs",
                            description = "This utility inspects squashfs/ext4/ISO partitions inside .iso, .vmdk, and .vhd files, strips obsolete hardware kernels/bootloaders, and packages userland packages into standard OCI tarball layers that run natively inside Podman with zero hypervisor overhead.",
                            cliCommand = "podman-convert --source alpine-virt.iso --target alpine:latest --simd-neon",
                            keyFeatures = listOf(
                                "Auto-detects Debian, Ubuntu, Alpine, Arch, and Fedora installers",
                                "Bypasses slow QEMU hypervisors by running extracted binaries natively",
                                "ARM NEON 128-bit SIMD accelerated layer decompression (3.8x faster)",
                                "Zero root privilege required"
                            ),
                            bestPractice = "Best used with lightweight Alpine or Debian cloud-init ISOs. Ensure your host has at least 2 GB free internal storage during layer extraction."
                        )
                    )
                },
                actionLabel = "Convert",
                onActionClick = { viewModel.setShowConvertDialog(true) }
            )
        }

        // Tool 2: Disk Image Builder (QCOW2, VHD, VMDK, RAW)
        item {
            ToolCardItem(
                icon = Icons.Default.Storage,
                iconBg = Color(0xFFFEF3C7),
                iconTint = Color(0xFFD97706),
                title = "Disk Image Builder",
                subtitle = "Generate blank .qcow2, .vhd, .img & .vmdk files with custom partition formats & block sizes",
                badge = "qemu-img / mkfs",
                badgeColor = Color(0xFFD97706),
                onInfoClick = {
                    viewModel.showToolInfo(
                        ToolInfoData(
                            title = "Blank Disk Image Builder",
                            subtitle = "Create virtual disk images (QCOW2, VHD, VMDK, RAW) with custom filesystems",
                            description = "Enables creation of virtual drive files formatted as EXT4, NTFS, BTRFS, FAT32, or XFS. Supports sparse dynamic allocation so a 64 GB virtual disk consumes only minimal space initially.",
                            cliCommand = "qemu-img create -f qcow2 -o cluster_size=4k,preallocation=metadata disk.qcow2 16G && mkfs.ext4 -b 4096 disk.qcow2",
                            keyFeatures = listOf(
                                "Supported formats: QCOW2 (Copy-on-write), VHD (Virtual Hard Disk), VMDK (VMware), RAW (.img)",
                                "Filesystems: EXT4 (Linux fast), NTFS (Windows compatible), BTRFS (snapshots), FAT32, XFS",
                                "Customizable cluster block sizes: 512B, 4KB (Flash standard), 64KB, 1MB (High throughput)",
                                "Sparse allocation preserves host device storage"
                            ),
                            bestPractice = "Use QCOW2 with 4KB block size for Linux container persistent data. Use VHD with NTFS for shared cross-platform storage drives."
                        )
                    )
                },
                actionLabel = "Create Disk",
                onActionClick = { viewModel.setShowCreateDiskDialog(true) }
            )
        }

        // Tool 3: Docker & OCI Image Pusher
        item {
            ToolCardItem(
                icon = Icons.Default.CloudUpload,
                iconBg = Color(0xFFDCFCE7),
                iconTint = Color(0xFF15803D),
                title = "Push Image to Registry",
                subtitle = "Publish local container images to Docker Hub, Quay.io, or GitHub Container Registry (GHCR)",
                badge = "Docker / OCI Registry",
                badgeColor = Color(0xFF15803D),
                onInfoClick = {
                    viewModel.showToolInfo(
                        ToolInfoData(
                            title = "Publish & Push Container Images",
                            subtitle = "Upload custom configured container rootfs to remote container registries",
                            description = "Push your customized containers directly to Docker Hub (docker.io), Quay.io, or GitHub Packages (ghcr.io) without leaving Android. Uses rootless HTTPS streaming with vector SHA-256 layer hashing.",
                            cliCommand = "podman push localhost/my-container:latest docker://docker.io/username/repo:tag",
                            keyFeatures = listOf(
                                "Supports Docker Hub, Quay.io, GitHub GHCR, and self-hosted private registries",
                                "OCI distribution spec compliant streaming upload",
                                "Hardware-accelerated SIMD digest verification",
                                "Multi-arch aarch64 manifest builder"
                            ),
                            bestPractice = "Generate a personal access token (PAT) from your Docker Hub or GitHub account and input it when prompted for authentication."
                        )
                    )
                },
                actionLabel = "Push Image",
                onActionClick = { viewModel.setShowPushDockerDialog(true) }
            )
        }

        // Tool 4: TCP/UDP Port Bridge
        item {
            ToolCardItem(
                icon = Icons.Default.Router,
                iconBg = Color(0xFFF3E8FF),
                iconTint = Color(0xFF7E22CE),
                title = "Port Bridge & Proxy",
                subtitle = "Forward internal container network ports (e.g., 80, 22, 5901, 8888) to Android host",
                badge = "Rootless Proxy",
                badgeColor = Color(0xFF7E22CE),
                onInfoClick = {
                    viewModel.showToolInfo(
                        ToolInfoData(
                            title = "TCP / UDP Network Port Bridge",
                            subtitle = "Expose container web servers, SSH daemons, and Jupyter notebooks to your local WiFi",
                            description = "Since Android runs containers in rootless user namespaces, privileged ports (< 1024) are bridged via slirp4netns/pasta proxy to unprivileged high ports (> 1024) on the Android host.",
                            cliCommand = "podman run -p 8080:80 -p 5901:5901 debian:trixie",
                            keyFeatures = listOf(
                                "Expose web servers (Nginx/Apache), SSH, Node.js, and JupyterLab",
                                "Zero root or iptables required",
                                "Automatic connection keep-alive",
                                "Supports TCP and UDP traffic routing"
                            ),
                            bestPractice = "Use host ports above 1024 (like 8080 or 8443) since Android does not grant apps permission to bind raw low ports directly."
                        )
                    )
                },
                actionLabel = "Add Rule",
                onActionClick = { viewModel.setShowPortBridgeDialog(true) }
            )
        }

        // Tool 5: Rootless Tarball / Squashfs Export
        item {
            ToolCardItem(
                icon = Icons.Default.FolderZip,
                iconBg = Color(0xFFFEE2E2),
                iconTint = Color(0xFFB91C1C),
                title = "Rootfs Exporter",
                subtitle = "Export container file system as compressed .tar.zst or read-only .squashfs archive",
                badge = "Rootless Backup",
                badgeColor = Color(0xFFB91C1C),
                onInfoClick = {
                    viewModel.showToolInfo(
                        ToolInfoData(
                            title = "Container Rootfs Backup & Export",
                            subtitle = "Package container environments into portable compressed archives",
                            description = "Export any installed container into a portable .tar.zst archive or read-only squashfs image that can be restored on any Android device, Termux, PRoot, or Linux workstation.",
                            cliCommand = "podman export debian-trixie | zstd -T0 -3 > debian-backup.tar.zst",
                            keyFeatures = listOf(
                                "Zstandard multi-threaded compression with NEON vectorization",
                                "Portable across PRoot, Termux, Podman, and Docker",
                                "Safely preserves user file permissions and packages",
                                "Exports directly to external Android storage"
                            ),
                            bestPractice = "Export a backup before making heavy system package upgrades inside your Linux container."
                        )
                    )
                },
                actionLabel = "Export",
                onActionClick = {
                    val activeSys = allSystems.firstOrNull { it.isInstalled }
                    if (activeSys != null) {
                        viewModel.startConversion(activeSys.name, "CONTAINER_ROOTFS", "${activeSys.id}-backup", "Local Tarball", true)
                    } else {
                        viewModel.setShowConvertDialog(true)
                    }
                }
            )
        }

        // Tool 6: PRoot-Distro Engine & Podman Fallback
        item {
            ToolCardItem(
                icon = Icons.Default.Shield,
                iconBg = Color(0xFFFEF3C7),
                iconTint = Color(0xFFD97706),
                title = "PRoot-Distro Fallback Engine",
                subtitle = "Userland ptrace syscall interception engine that guarantees 100% compatibility when Podman encounters kernel restrictions",
                badge = "Kernel Resilience Guard",
                badgeColor = Color(0xFFD97706),
                onInfoClick = {
                    viewModel.showToolInfo(
                        ToolInfoData(
                            title = "PRoot-Distro Container Fallback",
                            subtitle = "Dual-engine unprivileged execution with automatic ptrace failover",
                            description = "While Podman leverages native Linux user namespaces (crun) for bare-metal performance, some Android vendor kernels disable CONFIG_USER_NS or enforce strict seccomp clone3 policies. Dockbox automatically detects these restrictions and transparently switches the container into PRoot-Distro ptrace syscall interception mode so your Linux environment always runs reliably.",
                            cliCommand = "proot-distro login --bind /sdcard:/sdcard ubuntu --isolated",
                            keyFeatures = listOf(
                                "Zero root permissions required on any Android 8.0+ device",
                                "Bypasses missing CONFIG_USER_NS and restricted clone3 syscalls",
                                "Emulates chroot, fake root UID 0, and /proc /sys filesystem mounts",
                                "Preserves container rootfs state and package configurations across engine switches"
                            ),
                            bestPractice = "Leave 'Auto PRoot Fallback Guard' enabled in container settings for automatic failover, or switch engines manually in the terminal with 'switch-engine'."
                        )
                    )
                },
                actionLabel = "Inspect",
                onActionClick = {
                    viewModel.selectTab(MainTab.TERMINAL)
                    viewModel.executeTerminalCommand("proot-info")
                }
            )
        }

        // Section: Managed Items Tab Bar (Disks, Conversions, Pushes, Ports)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolFilterChip(
                    label = "Disks (${diskImages.size})",
                    selected = selectedSection == "DISKS",
                    onClick = { selectedSection = "DISKS" }
                )
                ToolFilterChip(
                    label = "Jobs (${conversions.size})",
                    selected = selectedSection == "CONVERSIONS",
                    onClick = { selectedSection = "CONVERSIONS" }
                )
                ToolFilterChip(
                    label = "Pushes (${dockerPushes.size})",
                    selected = selectedSection == "PUSHES",
                    onClick = { selectedSection = "PUSHES" }
                )
                ToolFilterChip(
                    label = "Ports (${portRules.size})",
                    selected = selectedSection == "PORTS",
                    onClick = { selectedSection = "PORTS" }
                )
            }
        }

        // Display Selected Section Content
        when (selectedSection) {
            "DISKS" -> {
                if (diskImages.isEmpty()) {
                    item {
                        EmptyToolsPlaceholder(
                            icon = Icons.Default.Storage,
                            title = "No disk image files created",
                            subtitle = "Tap 'Create Disk' above to generate blank QCOW2, VHD, or VMDK drives."
                        )
                    }
                } else {
                    items(diskImages, key = { it.id }) { disk ->
                        DiskImageCard(
                            disk = disk,
                            onDelete = { viewModel.deleteDiskImage(disk.id) }
                        )
                    }
                }
            }

            "CONVERSIONS" -> {
                if (conversions.isEmpty()) {
                    item {
                        EmptyToolsPlaceholder(
                            icon = Icons.Default.Transform,
                            title = "No conversion jobs active",
                            subtitle = "Convert ISO or VMDK images into unprivileged container layers."
                        )
                    }
                } else {
                    items(conversions, key = { it.id }) { job ->
                        ConversionJobCard(job = job)
                    }
                }
            }

            "PUSHES" -> {
                if (dockerPushes.isEmpty()) {
                    item {
                        EmptyToolsPlaceholder(
                            icon = Icons.Default.CloudUpload,
                            title = "No registry uploads recorded",
                            subtitle = "Push local container images to Docker Hub, Quay.io, or GitHub GHCR."
                        )
                    }
                } else {
                    items(dockerPushes, key = { it.id }) { push ->
                        DockerPushCard(item = push)
                    }
                }
            }

            "PORTS" -> {
                if (portRules.isEmpty()) {
                    item {
                        EmptyToolsPlaceholder(
                            icon = Icons.Default.Router,
                            title = "No port forwarding rules active",
                            subtitle = "Forward internal container daemon ports to Android host ports."
                        )
                    }
                } else {
                    items(portRules, key = { it.id }) { rule ->
                        PortRuleCard(
                            rule = rule,
                            onRemove = { viewModel.removePortBridgeRule(rule.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateDiskDialog) {
        CreateDiskDialog(
            onDismiss = { viewModel.setShowCreateDiskDialog(false) },
            onCreate = { name, format, partType, sizeGb, blockSize, prealloc ->
                viewModel.createDiskImage(name, format, partType, sizeGb, blockSize, prealloc)
            }
        )
    }

    if (showPushDockerDialog) {
        PushDockerDialog(
            allSystems = installedSystems,
            onDismiss = { viewModel.setShowPushDockerDialog(false) },
            onPush = { img, reg, repo, tag, user ->
                viewModel.pushDockerImage(img, reg, repo, tag, user)
            }
        )
    }

    if (showPortBridgeDialog) {
        AddPortBridgeDialog(
            allSystems = installedSystems,
            onDismiss = { viewModel.setShowPortBridgeDialog(false) },
            onAdd = { cid, hp, cp, proto ->
                viewModel.addPortBridgeRule(cid, hp, cp, proto)
            }
        )
    }

    if (showConvertDialog) {
        NewConversionDialog(
            onDismiss = { viewModel.setShowConvertDialog(false) },
            onStart = { src, fmt, tgt, reg, simd ->
                viewModel.startConversion(src, fmt, tgt, reg, simd)
            }
        )
    }

    selectedToolInfo?.let { info ->
        ToolExplanationDialog(
            info = info,
            onDismiss = { viewModel.showToolInfo(null) }
        )
    }
}

@Composable
fun ToolCardItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    onInfoClick: () -> Unit,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = UDroidTextPrimary
                            )
                            IconButton(
                                onClick = onInfoClick,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Info about $title",
                                    tint = UDroidTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = UDroidTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ToolFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (selected) UDroidGreen else Color.White,
        border = BorderStroke(1.dp, if (selected) UDroidGreen else UDroidCardBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) Color.White else UDroidTextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun DiskImageCard(disk: DiskImageItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                }

                Column {
                    Text(
                        text = disk.fileName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = UDroidTextPrimary
                    )
                    Text(
                        text = "${disk.format} • ${disk.partitionType} • ${disk.sizeGb} GB • Block: ${disk.blockSize}",
                        fontSize = 11.sp,
                        color = UDroidTextSecondary
                    )
                    Text(
                        text = disk.path,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = UDroidTextMuted,
                        maxLines = 1
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete disk", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DockerPushCard(item: DockerPushItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "${item.repository}:${item.tag}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = UDroidTextPrimary
                        )
                        Text(
                            text = "${item.registry} • ${item.imageName}",
                            fontSize = 11.sp,
                            color = UDroidTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }
            }

            if (item.progress < 1.0f) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = UDroidGreen,
                    trackColor = Color(0xFFE2E8F0)
                )
            }
        }
    }
}

@Composable
fun PortRuleCard(rule: PortBridgeRule, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Router, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "Host 0.0.0.0:${rule.hostPort} ➔ Container ${rule.containerPort}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = UDroidTextPrimary
                    )
                    Text(
                        text = "${rule.containerName} (${rule.protocol}) • ${rule.status}",
                        fontSize = 11.sp,
                        color = UDroidTextSecondary
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove rule", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun EmptyToolsPlaceholder(icon: ImageVector, title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = UDroidTextMuted, modifier = Modifier.size(36.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = UDroidTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun ToolExplanationDialog(
    info: ToolInfoData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = UDroidGreen)
                Text(text = info.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(text = info.subtitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UDroidTextPrimary)
                }

                item {
                    Text(text = info.description, fontSize = 12.sp, color = UDroidTextSecondary, lineHeight = 17.sp)
                }

                item {
                    Text(text = "CLI EQUIVALENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UDroidTextMuted)
                    Surface(
                        color = UDroidDarkSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = info.cliCommand,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = UDroidTerminalCyan,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                item {
                    Text(text = "KEY CAPABILITIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UDroidTextMuted)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        info.keyFeatures.forEach { feat ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = UDroidGreen, modifier = Modifier.size(14.dp))
                                Text(text = feat, fontSize = 11.sp, color = UDroidTextPrimary)
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF5F2))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = UDroidGreen, modifier = Modifier.size(14.dp))
                            Text(text = info.bestPractice, fontSize = 11.sp, color = UDroidTextPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", fontWeight = FontWeight.Bold, color = UDroidGreen)
            }
        }
    )
}

@Composable
fun CreateDiskDialog(
    onDismiss: () -> Unit,
    onCreate: (fileName: String, format: String, partitionType: String, sizeGb: Int, blockSize: String, prealloc: Boolean) -> Unit
) {
    var fileName by remember { mutableStateOf("linux-storage") }
    var selectedFormat by remember { mutableStateOf("QCOW2") }
    var selectedPartType by remember { mutableStateOf("EXT4") }
    var sizeGb by remember { mutableFloatStateOf(16f) }
    var selectedBlockSize by remember { mutableStateOf("4KB") }
    var preallocate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Blank Disk Image", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Disk Image Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Format Selector
                Text("Disk Format", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("QCOW2", "VHD", "VMDK", "RAW").forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt, fontSize = 11.sp) }
                        )
                    }
                }

                // Partition Type
                Text("Partition Filesystem", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("EXT4", "NTFS", "BTRFS", "FAT32", "XFS").forEach { pt ->
                        FilterChip(
                            selected = selectedPartType == pt,
                            onClick = { selectedPartType = pt },
                            label = { Text(pt, fontSize = 11.sp) }
                        )
                    }
                }

                // Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Disk Capacity", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${sizeGb.toInt()} GB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidGreen)
                }
                Slider(
                    value = sizeGb,
                    onValueChange = { sizeGb = it },
                    valueRange = 1f..128f,
                    steps = 127
                )

                // Block Size
                Text("Cluster / Block Size", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("512B", "4KB", "64KB", "1MB").forEach { bs ->
                        FilterChip(
                            selected = selectedBlockSize == bs,
                            onClick = { selectedBlockSize = bs },
                            label = { Text(bs, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = preallocate, onCheckedChange = { preallocate = it })
                    Text("Preallocate metadata (Sparse file)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        onCreate(fileName, selectedFormat, selectedPartType, sizeGb.toInt(), selectedBlockSize, preallocate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
            ) {
                Text("Build Disk", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PushDockerDialog(
    allSystems: List<com.example.data.local.entity.ContainerSystemEntity>,
    onDismiss: () -> Unit,
    onPush: (imgName: String, reg: String, repo: String, tag: String, user: String) -> Unit
) {
    var selectedImg by remember { mutableStateOf(allSystems.firstOrNull()?.name ?: "alpine-3.22") }
    var selectedRegistry by remember { mutableStateOf("Docker Hub (docker.io)") }
    var repositoryName by remember { mutableStateOf("myusername/dockbox-linux") }
    var tag by remember { mutableStateOf("latest") }
    var username by remember { mutableStateOf("myusername") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Push Image to Registry", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Target Container Registry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                listOf("Docker Hub (docker.io)", "Quay.io", "GitHub (ghcr.io)", "Custom").forEach { reg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRegistry = reg },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedRegistry == reg, onClick = { selectedRegistry = reg })
                        Text(reg, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = repositoryName,
                    onValueChange = { repositoryName = it },
                    label = { Text("Repository Path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it },
                        label = { Text("Tag") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Auth Username") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (repositoryName.isNotBlank()) {
                        onPush(selectedImg, selectedRegistry, repositoryName, tag, username)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
            ) {
                Text("Start Push", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddPortBridgeDialog(
    allSystems: List<com.example.data.local.entity.ContainerSystemEntity>,
    onDismiss: () -> Unit,
    onAdd: (containerId: String, hostPort: Int, containerPort: Int, protocol: String) -> Unit
) {
    var selectedContainerId by remember { mutableStateOf(allSystems.firstOrNull()?.id ?: "debian-trixie") }
    var hostPortText by remember { mutableStateOf("8080") }
    var containerPortText by remember { mutableStateOf("80") }
    var protocol by remember { mutableStateOf("TCP") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Port Forwarding Rule", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Container", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (allSystems.isEmpty()) {
                    Text("No installed containers found.", fontSize = 12.sp, color = UDroidTextSecondary)
                } else {
                    allSystems.forEach { sys ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedContainerId = sys.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedContainerId == sys.id, onClick = { selectedContainerId = sys.id })
                            Text(sys.name, fontSize = 13.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hostPortText,
                        onValueChange = { hostPortText = it },
                        label = { Text("Host Port") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = containerPortText,
                        onValueChange = { containerPortText = it },
                        label = { Text("Container Port") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TCP", "UDP", "TCP+UDP").forEach { proto ->
                        FilterChip(
                            selected = protocol == proto,
                            onClick = { protocol = proto },
                            label = { Text(proto, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hp = hostPortText.toIntOrNull() ?: 8080
                    val cp = containerPortText.toIntOrNull() ?: 80
                    onAdd(selectedContainerId, hp, cp, protocol)
                },
                colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
            ) {
                Text("Enable Bridge", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
