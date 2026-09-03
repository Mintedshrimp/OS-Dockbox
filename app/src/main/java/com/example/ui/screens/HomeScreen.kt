package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
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
import com.example.domain.model.SystemSpec
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel
import com.example.ui.viewmodel.MainTab

@Composable
fun HomeScreen(
    viewModel: ContainerViewModel,
    modifier: Modifier = Modifier
) {
    val allSystems by viewModel.allSystems.collectAsStateWithLifecycle()
    val installed = remember(allSystems) { allSystems.filter { it.isInstalled } }
    val activeSystem = remember(installed) { installed.firstOrNull { it.isRunning } ?: installed.firstOrNull() }
    val spec by viewModel.systemSpec.collectAsStateWithLifecycle()

    var showCompatibilityDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UDroidBg),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            HeaderBar(
                title = "OS Dockbox",
                statusText = if (activeSystem?.isRunning == true) "SESSION LIVE" else "PODMAN READY",
                version = "v0.1.1"
            )
        }

        // Home Title & Subtitle + Refresh Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Home",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = UDroidTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Device specs, hardware acceleration, and tool suite",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UDroidTextSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.runNeonBenchmark() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh status",
                        tint = UDroidTextSecondary
                    )
                }
            }
        }

        // Active Container Live Card (if any container is installed)
        if (activeSystem != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeSystem.isRunning) Color(0xFFF0FDF4) else Color.White
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (activeSystem.isRunning) Color(0xFF86EFAC) else UDroidCardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (activeSystem.isRunning) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = "Container Icon",
                                        tint = if (activeSystem.isRunning) Color(0xFF15803D) else UDroidTextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = activeSystem.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = UDroidTextPrimary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (activeSystem.isRunning) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (activeSystem.isRunning) "RUNNING" else "STOPPED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (activeSystem.isRunning) Color(0xFF15803D) else UDroidTextSecondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${activeSystem.engineType} • ${activeSystem.flavor} • ${activeSystem.diskUsageMb} MB",
                                        fontSize = 12.sp,
                                        color = UDroidTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.toggleSystem(activeSystem)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeSystem.isRunning) Color(0xFFDC2626) else UDroidGreen
                                )
                            ) {
                                Icon(
                                    imageVector = if (activeSystem.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeSystem.isRunning) "Stop" else "Start",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.setActiveContainer(activeSystem.id)
                                    viewModel.selectTab(MainTab.TERMINAL)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = UDroidGreen),
                                border = BorderStroke(1.dp, UDroidGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Terminal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.setActiveContainer(activeSystem.id)
                                    viewModel.setDesktopWindowMode(com.example.ui.viewmodel.DesktopWindowMode.FULLSCREEN)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFE0F2FE),
                                    contentColor = Color(0xFF0369A1)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DesktopWindows,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("GUI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TOP HALF: HARDWARE SPECS & SYSTEM STATUS
        // ==========================================

        // Section Title: CURRENT DEVICE SPECS
        item {
            Text(
                text = "CURRENT DEVICE SPECS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = UDroidTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 4.dp)
            )
        }

        // Top Row: Device Model, OS & Architecture Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, UDroidCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = "Device",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${spec.deviceManufacturer} • ${spec.deviceModel}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UDroidTextPrimary
                                )
                                Text(
                                    text = "${spec.androidVersion} • ${spec.kernelVersion}",
                                    fontSize = 12.sp,
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
                                text = "aarch64",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecStatItem(label = "Instruction Set", value = "ARMv8.2-A Kryo")
                        SpecStatItem(label = "ABI", value = "arm64-v8a (64-bit)")
                        SpecStatItem(label = "Isolation", value = "Podman / PRoot Guard")
                    }
                }
            }
        }

        // Just Underneath: RAM Usage Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, UDroidCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = "RAM",
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "RAM Usage",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UDroidTextPrimary
                                )
                                Text(
                                    text = "${spec.ramUsedMb} MB used / ${spec.ramTotalMb} MB total",
                                    fontSize = 12.sp,
                                    color = UDroidTextSecondary
                                )
                            }
                        }

                        Text(
                            text = "${((spec.ramUsedMb.toFloat() / spec.ramTotalMb.toFloat()) * 100).toInt()}% Used",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = UDroidGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { spec.ramUsedMb.toFloat() / spec.ramTotalMb.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = UDroidGreen,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Free RAM: ${spec.memoryFreeGb} GB",
                            fontSize = 11.sp,
                            color = UDroidTextSecondary
                        )
                        Text(
                            text = "OverlayFS: Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }
        }

        // Left / Right Grid: CPU Usage, GPU Usage, Vulkan Version, OpenGL Version
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // CPU Usage Card
                HardwareMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Speed,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = "CPU Usage",
                    primaryValue = "${spec.cpuUsagePercent}% Load",
                    subtitle = "${spec.cpuCores} Cores (${spec.cpuFrequency})",
                    tagText = "NEON SIMD",
                    tagBg = Color(0xFFDCFCE7),
                    tagTint = Color(0xFF15803D)
                )

                // GPU Usage Card
                HardwareMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DeveloperBoard,
                    iconBg = Color(0xFFFAF5FF),
                    iconTint = Color(0xFF9333EA),
                    title = "GPU Usage",
                    primaryValue = "${spec.gpuUsagePercent}% Load",
                    subtitle = spec.gpuRenderer,
                    tagText = "Hardware Accel",
                    tagBg = Color(0xFFF3E8FF),
                    tagTint = Color(0xFF7E22CE)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Vulkan API Version Card
                HardwareMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AutoAwesome,
                    iconBg = Color(0xFFFFF1F2),
                    iconTint = Color(0xFFE11D48),
                    title = "Vulkan API",
                    primaryValue = spec.vulkanVersion,
                    subtitle = "Hardware Compute & Raster",
                    tagText = "VK 1.3 Ready",
                    tagBg = Color(0xFFFFE4E6),
                    tagTint = Color(0xFFBE123C)
                )

                // OpenGL ES Version Card
                HardwareMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Layers,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706),
                    title = "OpenGL ES",
                    primaryValue = spec.openGlVersion,
                    subtitle = "VirGL 3D Pass-Through",
                    tagText = "GLES 3.2",
                    tagBg = Color(0xFFFEF3C7),
                    tagTint = Color(0xFFB45309)
                )
            }
        }

        // ==========================================
        // BOTTOM HALF: TOOLS & UTILITIES SECTION
        // ==========================================

        item {
            Text(
                text = "TOOLS & UTILITIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = UDroidTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeNavCard(
                    icon = Icons.Default.Build,
                    iconBg = Color(0xFFEFF5F2),
                    iconTint = UDroidTextPrimary,
                    title = "Tools Suite",
                    subtitle = "ISO Converter, Disk Builder, Docker Push & Port Bridge",
                    badgeText = "5 Tools",
                    badgeColor = UDroidGreen,
                    onClick = { viewModel.selectTab(MainTab.TOOLS) }
                )

                HomeNavCard(
                    icon = Icons.Default.Storage,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706),
                    title = "Blank Disk Builder",
                    subtitle = "Make blank .qcow2, .vhd, .img (EXT4, NTFS, 4K/64K)",
                    badgeText = "Create",
                    badgeColor = Color(0xFFD97706),
                    onClick = {
                        viewModel.selectTab(MainTab.TOOLS)
                        viewModel.setShowCreateDiskDialog(true)
                    }
                )

                HomeNavCard(
                    icon = Icons.Default.Transform,
                    iconBg = Color(0xFFE0F2FE),
                    iconTint = Color(0xFF0284C7),
                    title = "ISO & VMDK Converter",
                    subtitle = "Extract rootfs from ISO, VMDK, VHD to OCI containers",
                    badgeText = "Convert",
                    badgeColor = Color(0xFF0284C7),
                    onClick = {
                        viewModel.selectTab(MainTab.TOOLS)
                        viewModel.setShowConvertDialog(true)
                    }
                )

                HomeNavCard(
                    icon = Icons.Default.CloudUpload,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF15803D),
                    title = "Publish & Push Container",
                    subtitle = "Push images to Docker Hub, Quay.io, or GitHub GHCR",
                    badgeText = "Push",
                    badgeColor = Color(0xFF15803D),
                    onClick = {
                        viewModel.selectTab(MainTab.TOOLS)
                        viewModel.setShowPushDockerDialog(true)
                    }
                )
            }
        }

        // Systems & Terminal Section
        item {
            Text(
                text = "CONTAINERS & SESSIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = UDroidTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeNavCard(
                    icon = Icons.Default.Dns,
                    iconBg = Color(0xFFEFF5F2),
                    iconTint = UDroidTextPrimary,
                    title = "OS",
                    subtitle = activeSystem?.let { "${it.name} (Active)" } ?: if (installed.isEmpty()) "Search & install OS containers" else "${installed.size} OS containers installed",
                    badgeText = if (installed.isNotEmpty()) "${installed.size} Installed" else "Install",
                    badgeColor = if (installed.isNotEmpty()) Color(0xFF15803D) else UDroidGreen,
                    onClick = { viewModel.selectTab(MainTab.OS) }
                )

                HomeNavCard(
                    icon = Icons.Default.Terminal,
                    iconBg = Color(0xFFEFF5F2),
                    iconTint = UDroidTextPrimary,
                    title = "Terminal",
                    subtitle = activeSystem?.let { "Live shell attached to ${it.name}" } ?: "Opens container shell when launched",
                    badgeText = if (activeSystem?.isRunning == true) "Active" else "Ready",
                    badgeColor = if (activeSystem?.isRunning == true) Color(0xFF15803D) else UDroidTextSecondary,
                    onClick = { viewModel.selectTab(MainTab.TERMINAL) }
                )

                HomeNavCard(
                    icon = Icons.Default.DesktopWindows,
                    iconBg = Color(0xFFEFF5F2),
                    iconTint = UDroidTextPrimary,
                    title = "Interactive Desktop",
                    subtitle = "X11/Wayland Desktop with Fullscreen, Freeform & PiP modes",
                    badgeText = "PiP / Windowed",
                    badgeColor = UDroidGreen,
                    onClick = { viewModel.setDesktopWindowMode(com.example.ui.viewmodel.DesktopWindowMode.FULLSCREEN) }
                )

                HomeNavCard(
                    icon = Icons.Default.Info,
                    iconBg = Color(0xFFEFF5F2),
                    iconTint = UDroidTextPrimary,
                    title = "About OS Dockbox",
                    subtitle = "Runtime journal, supervisor logs, and engine details",
                    onClick = { viewModel.selectTab(MainTab.ABOUT) }
                )
            }
        }
    }

    if (showCompatibilityDialog) {
        CompatibilityDialog(
            spec = spec,
            onDismiss = { showCompatibilityDialog = false },
            onRunBench = { viewModel.runNeonBenchmark() }
        )
    }
}

@Composable
fun HardwareMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    primaryValue: String,
    subtitle: String,
    tagText: String,
    tagBg: Color,
    tagTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(18.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tagBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = tagText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tagTint)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = title, fontSize = 11.sp, color = UDroidTextMuted, fontWeight = FontWeight.SemiBold)
            Text(text = primaryValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
            Text(text = subtitle, fontSize = 10.sp, color = UDroidTextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun HomeNavCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = UDroidTextSecondary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = UDroidTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = UDroidTextSecondary,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (badgeText != null) {
                    Text(
                        text = badgeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Navigate",
                    tint = UDroidTextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun SpecStatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = UDroidTextMuted,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = UDroidTextPrimary
        )
    }
}

@Composable
fun CompatibilityDialog(
    spec: SystemSpec,
    onDismiss: () -> Unit,
    onRunBench: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = UDroidGreen)
                Text(
                    text = "Device Compatibility & Engine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Host Architecture: ${spec.architecture}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Kernel: ${spec.kernelVersion}",
                    fontSize = 12.sp,
                    color = UDroidTextSecondary
                )
                Text(
                    text = "SIMD: ${spec.neonSimdStatus}",
                    fontSize = 12.sp,
                    color = Color(0xFF0284C7),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vulkan: ${spec.vulkanVersion}",
                    fontSize = 12.sp,
                    color = UDroidTextSecondary
                )
                Text(
                    text = "OpenGL: ${spec.openGlVersion}",
                    fontSize = 12.sp,
                    color = UDroidTextSecondary
                )
                Text(
                    text = "Rootless Engine: Podman 5.0.3 (crun)",
                    fontSize = 12.sp,
                    color = UDroidGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRunBench()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
            ) {
                Text("Run NEON SIMD Benchmark", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = UDroidTextSecondary)
            }
        }
    )
}
