package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ContainerAppEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel
import com.example.ui.viewmodel.DesktopWindowMode

@Composable
fun AppsScreen(
    viewModel: ContainerViewModel,
    modifier: Modifier = Modifier
) {
    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val allSystems by viewModel.allSystems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQueryApps.collectAsStateWithLifecycle()
    val activeContainerId by viewModel.activeContainerId.collectAsStateWithLifecycle()
    val appTypeFilter by viewModel.appTypeFilter.collectAsStateWithLifecycle()
    val diskImages by viewModel.diskImages.collectAsStateWithLifecycle()

    val showAddAppDialog by viewModel.showAddAppDialog.collectAsStateWithLifecycle()
    val showRootfsInstallerDialog by viewModel.showRootfsInstallerDialog.collectAsStateWithLifecycle()
    val showCreateDiskDialog by viewModel.showCreateDiskDialog.collectAsStateWithLifecycle()
    val showDesktopViewer by viewModel.showDesktopViewer.collectAsStateWithLifecycle()

    var showNetworkDiagnostics by remember { mutableStateOf(false) }
    var showDiskManager by remember { mutableStateOf(false) }
    var interactiveAppLaunch by remember { mutableStateOf<String?>(null) }

    val installedContainers = remember(allSystems) { allSystems.filter { it.isInstalled } }
    val activeSystem = remember(installedContainers, activeContainerId) {
        installedContainers.firstOrNull { it.id == activeContainerId } ?: installedContainers.firstOrNull()
    }

    // Apps filtered by active container AND user/system filter AND search query
    val containerApps = remember(allApps, activeSystem) {
        if (activeSystem == null) allApps
        else allApps.filter { it.containerId == activeSystem.id }
    }

    val filteredApps = remember(containerApps, appTypeFilter, searchQuery) {
        containerApps.filter { app ->
            val matchesType = when (appTypeFilter) {
                "USER" -> app.isUserApp
                "SYSTEM" -> !app.isUserApp
                "RUNNING" -> app.isRunning
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                app.name.contains(searchQuery, ignoreCase = true) ||
                        app.category.contains(searchQuery, ignoreCase = true) ||
                        app.displayType.contains(searchQuery, ignoreCase = true) ||
                        app.description.contains(searchQuery, ignoreCase = true)
            }
            matchesType && matchesSearch
        }
    }

    val userAppCount = remember(containerApps) { containerApps.count { it.isUserApp } }
    val systemAppCount = remember(containerApps) { containerApps.count { !it.isUserApp } }
    val runningAppCount = remember(containerApps) { containerApps.count { it.isRunning } }

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

        // Title and Subtitle with Top Action Icons
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
                        text = "Apps & Mini OS",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = UDroidTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${activeSystem?.name ?: "All Distros"} • ${filteredApps.size} apps available",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UDroidTextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add Custom App Button
                    IconButton(
                        onClick = { viewModel.setShowAddAppDialog(true) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add User App",
                            tint = UDroidGreen
                        )
                    }

                    // Open Desktop Viewer Button
                    IconButton(
                        onClick = {
                            interactiveAppLaunch = null
                            viewModel.showDesktop(true)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DesktopWindows,
                            contentDescription = "Open Desktop GUI",
                            tint = UDroidGreen
                        )
                    }
                }
            }
        }

        // Quick Feature Launcher Bar (Interactive Desktop, Net Diagnostics, Disk Images, Root Installer)
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AppSuiteFeatureChip(
                        icon = Icons.Default.DesktopWindows,
                        iconBg = Color(0xFFEFF5F2),
                        iconTint = UDroidGreen,
                        label = "Desktop GUI",
                        tag = "X11/VNC",
                        onClick = {
                            interactiveAppLaunch = "Chromium"
                            viewModel.showDesktop(true)
                        }
                    )
                }
                item {
                    AppSuiteFeatureChip(
                        icon = Icons.Default.Speed,
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7),
                        label = "Net Diagnostics",
                        tag = "Ping/Speed",
                        onClick = { showNetworkDiagnostics = true }
                    )
                }
                item {
                    AppSuiteFeatureChip(
                        icon = Icons.Default.Storage,
                        iconBg = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706),
                        label = "Disk Images",
                        tag = ".qcow2/SAF",
                        onClick = { showDiskManager = true }
                    )
                }
                item {
                    AppSuiteFeatureChip(
                        icon = Icons.Default.DownloadForOffline,
                        iconBg = Color(0xFFDCFCE7),
                        iconTint = Color(0xFF15803D),
                        label = "Root Installer",
                        tag = "Mini OS",
                        onClick = { viewModel.setShowRootfsInstallerDialog(true) }
                    )
                }
            }
        }

        // Container Selector Strip (View apps inside selected container)
        item {
            Column(modifier = Modifier.padding(start = 24.dp, top = 6.dp, bottom = 4.dp)) {
                Text(
                    text = "TARGET CONTAINER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = UDroidTextMuted,
                    letterSpacing = 1.sp
                )
            }
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(installedContainers, key = { it.id }) { container ->
                    val isSelected = (activeSystem?.id == container.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setActiveContainer(container.id) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (container.isRunning) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                }
                                Text(
                                    text = container.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE2EFE7),
                            selectedLabelColor = UDroidGreen,
                            containerColor = Color.White,
                            labelColor = UDroidTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) UDroidGreen else UDroidCardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // User vs System App Filter Tabs
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val appFilters = listOf(
                    "ALL" to "All Apps (${containerApps.size})",
                    "USER" to "User Apps ($userAppCount)",
                    "SYSTEM" to "System / Core ($systemAppCount)",
                    "RUNNING" to "Running ($runningAppCount)"
                )

                items(appFilters) { (filterKey, label) ->
                    val isSelected = appTypeFilter == filterKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setAppTypeFilter(filterKey) },
                        label = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (filterKey == "USER") Color(0xFFDCFCE7) else if (filterKey == "SYSTEM") Color(0xFFE0F2FE) else UDroidGreen,
                            selectedLabelColor = if (filterKey == "ALL") Color.White else UDroidTextPrimary,
                            containerColor = Color.White,
                            labelColor = UDroidTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) UDroidGreen else UDroidCardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQueryApps(it) },
                placeholder = {
                    Text(
                        text = "Search apps in ${activeSystem?.name ?: "container"}...",
                        color = UDroidTextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = UDroidTextMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQueryApps("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = UDroidTextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = UDroidGreen,
                    unfocusedBorderColor = UDroidCardBorder
                ),
                singleLine = true
            )
        }

        // Section header
        item {
            Column(modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = "${appTypeFilter.uppercase()} APPLICATIONS (${filteredApps.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = UDroidTextMuted,
                    letterSpacing = 1.sp
                )
            }
        }

        if (filteredApps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
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
                        Icon(Icons.Default.Apps, contentDescription = null, tint = UDroidTextMuted, modifier = Modifier.size(36.dp))
                        Text("No applications found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = UDroidTextPrimary)
                        Text(
                            text = "Tap '+ Add App' or switch target container to see installed packages.",
                            fontSize = 12.sp,
                            color = UDroidTextSecondary
                        )
                    }
                }
            }
        }

        // List of Apps
        items(filteredApps, key = { it.id }) { app ->
            AppItemCard(
                app = app,
                onToggle = { viewModel.toggleApp(app) },
                onLaunchFullscreen = {
                    viewModel.launchAppInDesktop(app.name, DesktopWindowMode.FULLSCREEN)
                },
                onLaunchFreeform = {
                    viewModel.launchAppInDesktop(app.name, DesktopWindowMode.FREEFORM)
                },
                onLaunchPip = {
                    viewModel.launchAppInDesktop(app.name, DesktopWindowMode.PIP)
                }
            )
        }
    }

    // Dialogs
    if (showNetworkDiagnostics) {
        NetworkDiagnosticsDialog(
            containerName = activeSystem?.name ?: "Container eth0",
            onDismiss = { showNetworkDiagnostics = false }
        )
    }

    if (showDiskManager) {
        DiskImageManagerDialog(
            diskImages = diskImages,
            onDismiss = { showDiskManager = false },
            onCreateDisk = {
                showDiskManager = false
                viewModel.setShowCreateDiskDialog(true)
            },
            onExportDisk = { disk -> viewModel.exportDiskFile(disk) },
            onDeleteDisk = { diskId -> viewModel.deleteDiskImage(diskId) }
        )
    }

    if (showAddAppDialog) {
        AddCustomAppDialog(
            installedContainers = installedContainers,
            selectedContainerId = activeSystem?.id,
            onDismiss = { viewModel.setShowAddAppDialog(false) },
            onAddApp = { containerId, name, category, command, displayType, description, port ->
                viewModel.addCustomUserApp(containerId, name, category, command, displayType, description, port)
            }
        )
    }

    if (showRootfsInstallerDialog) {
        RootInstallerDialog(
            onDismiss = { viewModel.setShowRootfsInstallerDialog(false) },
            onInstallRootfs = { name, fileName, sizeMb, engine ->
                viewModel.installCustomRootfsTarball(name, fileName, sizeMb, engine)
            }
        )
    }

    if (showCreateDiskDialog) {
        CreateDiskImageDialog(
            onDismiss = { viewModel.setShowCreateDiskDialog(false) },
            onCreateDisk = { fileName, format, partition, size, block, preallocate ->
                viewModel.createDiskImage(fileName, format, partition, size, block, preallocate)
            }
        )
    }
}

@Composable
fun AppSuiteFeatureChip(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = UDroidTextPrimary)
                Text(tag, fontSize = 10.sp, color = UDroidTextSecondary)
            }
        }
    }
}

@Composable
fun AppItemCard(
    app: ContainerAppEntity,
    onToggle: () -> Unit,
    onLaunchFullscreen: () -> Unit,
    onLaunchFreeform: () -> Unit,
    onLaunchPip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
        border = BorderStroke(1.dp, UDroidCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(getAppIconBg(app.category)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAppIconVector(app.name, app.category),
                        contentDescription = app.name,
                        tint = getAppIconTint(app.category),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = app.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = UDroidTextPrimary,
                            maxLines = 1
                        )
                        // User vs System Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (app.isUserApp) Color(0xFFDCFCE7) else Color(0xFFE2E8F0)
                        ) {
                            Text(
                                text = if (app.isUserApp) "USER" else "SYS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (app.isUserApp) Color(0xFF15803D) else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = if (app.description.isNotBlank()) app.description else getAppSubtitle(app.name),
                        fontSize = 11.sp,
                        color = UDroidTextSecondary,
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (app.displayType.contains("PORT")) Icons.Default.Language else if (app.displayType.contains("CLI")) Icons.Default.Terminal else Icons.Default.DesktopWindows,
                            contentDescription = null,
                            tint = UDroidTextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = app.displayType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UDroidTextMuted
                        )
                    }
                }
            }

            // Window Mode Launch Buttons & Main Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Freeform Floating Window Launch
                IconButton(
                    onClick = onLaunchFreeform,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FitScreen,
                        contentDescription = "Open Freeform",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // PiP Window Launch
                IconButton(
                    onClick = onLaunchPip,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPicture,
                        contentDescription = "Open PiP",
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Fullscreen Launch
                IconButton(
                    onClick = onLaunchFullscreen,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open in display",
                        tint = UDroidGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (app.isRunning) Color(0xFF14532D) else UDroidGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (app.isRunning) "Running" else "Launch",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getAppSubtitle(name: String): String {
    return when {
        name.contains("Network") -> "Manage and change network interfaces"
        name.contains("Chromium") -> "Hardware accelerated web browser"
        name.contains("Files") || name.contains("Nautilus") -> "Root filesystem file explorer"
        name.contains("Print") -> "CUPS printer daemon management"
        name.contains("UXTerm") -> "Unicode terminal emulator"
        name.contains("XTerm") -> "Standard X11 terminal client"
        name.contains("VS Code") -> "Code-server web IDE workspace"
        name.contains("HTOP") -> "Real-time process & CPU viewer"
        name.contains("GIMP") -> "GNU raster image editor"
        name.contains("Jupyter") -> "Computational data science notebooks"
        name.contains("Neovim") -> "Hyperextensible terminal code editor"
        name.contains("Git") -> "Distributed version control engine"
        name.contains("XFCE") -> "Lightweight desktop session"
        name.contains("Phosh") -> "Mobile-friendly Wayland shell"
        name.contains("FLWM") -> "Fast Light Window Manager"
        else -> "Containerized application utility"
    }
}

fun getAppIconBg(category: String): Color {
    return when (category.lowercase()) {
        "web" -> Color(0xFFE0F2FE)
        "development" -> Color(0xFFDCFCE7)
        "utility" -> Color(0xFFFEF3C7)
        else -> Color(0xFFE2EFE7)
    }
}

fun getAppIconTint(category: String): Color {
    return when (category.lowercase()) {
        "web" -> Color(0xFF0284C7)
        "development" -> Color(0xFF15803D)
        "utility" -> Color(0xFFD97706)
        else -> Color(0xFF1B7340)
    }
}

fun getAppIconVector(name: String, category: String): ImageVector {
    return when {
        name.contains("Network") -> Icons.Default.Settings
        name.contains("Chromium") -> Icons.Default.Language
        name.contains("Files") || name.contains("Nautilus") -> Icons.Default.Folder
        name.contains("Print") -> Icons.Default.Print
        name.contains("Term") || name.contains("Bash") || name.contains("Shell") -> Icons.Default.Terminal
        name.contains("VS Code") || name.contains("Neovim") || name.contains("Git") || name.contains("Notebook") || name.contains("Torch") -> Icons.Default.Code
        name.contains("HTOP") || name.contains("Speed") -> Icons.Default.Equalizer
        name.contains("GIMP") -> Icons.Default.Brush
        name.contains("XFCE") || name.contains("Phosh") || name.contains("FLWM") || name.contains("Desktop") -> Icons.Default.DesktopWindows
        else -> Icons.Default.Widgets
    }
}

