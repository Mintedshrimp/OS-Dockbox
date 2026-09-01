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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ContainerSystemEntity
import com.example.ui.components.DistroIcon
import com.example.ui.components.HeaderBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.BadgeType
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel
import com.example.ui.viewmodel.MainTab

@Composable
fun ContainersScreen(
    viewModel: ContainerViewModel,
    modifier: Modifier = Modifier
) {
    val allSystems by viewModel.allSystems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuerySystems.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.systemFilter.collectAsStateWithLifecycle()
    val installingId by viewModel.installingSystemId.collectAsStateWithLifecycle()
    val installProgress by viewModel.installProgress.collectAsStateWithLifecycle()
    val installStatusText by viewModel.installStatusText.collectAsStateWithLifecycle()
    val selectedForEdit by viewModel.selectedSystemForEdit.collectAsStateWithLifecycle()
    val showNewDialog by viewModel.showNewContainerDialog.collectAsStateWithLifecycle()

    val filteredList = remember(allSystems, searchQuery, selectedFilter) {
        allSystems.filter { system ->
            val matchesQuery = searchQuery.isBlank() ||
                    system.name.contains(searchQuery, ignoreCase = true) ||
                    system.flavor.contains(searchQuery, ignoreCase = true) ||
                    system.engineType.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "INSTALLED" -> system.isInstalled
                "PODMAN" -> system.engineType.contains("Podman")
                "PROOT" -> system.engineType.contains("PRoot")
                "RUNNING" -> system.isRunning
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    val installedCount = allSystems.count { it.isInstalled }
    val totalCount = allSystems.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UDroidBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setShowNewContainerDialog(true) },
                containerColor = UDroidGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add image") },
                text = { Text("Pull / Add Image", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                HeaderBar(
                    title = "OS Dockbox",
                    statusText = if (allSystems.any { it.isRunning }) "SESSION LIVE" else "PODMAN READY",
                    version = "v0.1.1"
                )
            }

            // Screen Header
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Text(
                        text = "Linux systems",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = UDroidTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "$installedCount installed • $totalCount compatible",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UDroidTextSecondary
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuerySystems(it) },
                    placeholder = {
                        Text(
                            text = "Search Ubuntu, Debian, Fedora...",
                            color = UDroidTextMuted,
                            fontSize = 14.sp
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
                            IconButton(onClick = { viewModel.setSearchQuerySystems("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = UDroidTextMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
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

            // Filter Chips
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All Systems",
                        "INSTALLED" to "Installed ($installedCount)",
                        "PODMAN" to "Podman Rootless",
                        "PROOT" to "PRoot Distros",
                        "RUNNING" to "Running (${allSystems.count { it.isRunning }})"
                    )

                    items(filters) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { viewModel.setSystemFilter(key) },
                            label = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE2EFE7),
                                selectedLabelColor = UDroidGreen,
                                containerColor = Color.White,
                                labelColor = UDroidTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == key,
                                borderColor = if (selectedFilter == key) UDroidGreen else UDroidCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Section Label
            item {
                Column(modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 8.dp)) {
                    Text(
                        text = "ALL SYSTEMS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UDroidTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "UDROID AND PROOT-DISTRO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UDroidTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // List of systems
            items(filteredList, key = { it.id }) { system ->
                ContainerSystemCard(
                    system = system,
                    onOpenTerminal = {
                        viewModel.setActiveContainer(system.id)
                        viewModel.selectTab(MainTab.TERMINAL)
                    },
                    onOpenConfig = { viewModel.openEditSystem(system) },
                    onInstall = { viewModel.installSystem(system.id) },
                    onToggleRunning = { viewModel.toggleSystem(system) }
                )
            }

            // Official Images section info banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, UDroidCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Docker Hub & Podman Registries",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = UDroidTextPrimary
                            )
                            Text(
                                text = "Can install any container directly via OCI rootless pull",
                                fontSize = 12.sp,
                                color = UDroidTextSecondary
                            )
                        }
                        Button(
                            onClick = { viewModel.setShowNewContainerDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Pull Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Install Progress Dialog
    if (installingId != null) {
        AlertDialog(
            onDismissRequest = { /* non-cancelable during install */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { installProgress },
                        modifier = Modifier.size(24.dp),
                        color = UDroidGreen
                    )
                    Text("Installing Container Image", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = installStatusText,
                        fontSize = 13.sp,
                        color = UDroidTextSecondary
                    )
                    LinearProgressIndicator(
                        progress = { installProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = UDroidGreen,
                        trackColor = Color(0xFFE2E9E5)
                    )
                    Text(
                        text = "${(installProgress * 100).toInt()}% • SIMD NEON decompressor active",
                        fontSize = 11.sp,
                        color = UDroidTextMuted
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Edit System Config Dialog
    selectedForEdit?.let { system ->
        SystemConfigDialog(
            system = system,
            onDismiss = { viewModel.openEditSystem(null) },
            onSave = { updated -> viewModel.saveSystemConfig(updated) },
            onDelete = {
                viewModel.deleteSystem(system.id)
                viewModel.openEditSystem(null)
            },
            onOpenTerminal = {
                viewModel.openEditSystem(null)
                viewModel.setActiveContainer(system.id)
                viewModel.selectTab(MainTab.TERMINAL)
            }
        )
    }

    // Add / Pull Image Dialog
    if (showNewDialog) {
        PullImageDialog(
            onDismiss = { viewModel.setShowNewContainerDialog(false) },
            onPull = { name, image, engine, ports, simd ->
                viewModel.createCustomContainer(name, image, engine, ports, simd)
            }
        )
    }
}

@Composable
fun ContainerSystemCard(
    system: ContainerSystemEntity,
    onOpenTerminal: () -> Unit,
    onOpenConfig: () -> Unit,
    onInstall: () -> Unit,
    onToggleRunning: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clickable { onOpenConfig() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                DistroIcon(flavor = system.flavor)

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = system.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = UDroidTextPrimary
                        )

                        if (system.isRunning) {
                            StatusBadge(text = "Active", type = BadgeType.ACTIVE)
                        } else if (system.isRecommended) {
                            StatusBadge(text = "Recommended", type = BadgeType.RECOMMENDED)
                        }
                    }

                    Text(
                        text = "${system.desktopEnv} • ${system.architecture} • ${if (system.engineType.contains("Podman")) "OS Dockbox" else "proot-distro"}",
                        fontSize = 12.sp,
                        color = UDroidTextSecondary
                    )
                }
            }

            // Trailing action or chevron
            if (system.isInstalled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = onOpenTerminal,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (system.isRunning) "Open" else "Start",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (system.isRunning) UDroidGreen else UDroidTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = if (system.isRunning) UDroidGreen else UDroidTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Details",
                    tint = UDroidTextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SystemConfigDialog(
    system: ContainerSystemEntity,
    onDismiss: () -> Unit,
    onSave: (ContainerSystemEntity) -> Unit,
    onDelete: () -> Unit,
    onOpenTerminal: () -> Unit
) {
    var engine by remember { mutableStateOf(system.engineType) }
    var ports by remember { mutableStateOf(system.portMappings) }
    var simd by remember { mutableStateOf(system.neonSimdEnabled) }
    var ramMb by remember { mutableStateOf(system.memoryLimitMb.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DistroIcon(flavor = system.flavor)
                Column {
                    Text(text = system.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "Container Engine Settings", fontSize = 12.sp, color = UDroidTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Engine Selector
                Text("Execution Engine:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Podman Rootless", "PRoot-Distro", "QEMU MicroVM").forEach { opt ->
                        FilterChip(
                            selected = engine == opt,
                            onClick = { engine = opt },
                            label = { Text(opt, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCFCE7),
                                selectedLabelColor = UDroidGreen
                            )
                        )
                    }
                }

                // SIMD Accelerator Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("NEON SIMD Acceleration", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("3.8x faster layer extraction and memcpy", fontSize = 11.sp, color = UDroidTextSecondary)
                    }
                    Switch(
                        checked = simd,
                        onCheckedChange = { simd = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = UDroidGreen
                        )
                    )
                }

                // Port mappings
                OutlinedTextField(
                    value = ports,
                    onValueChange = { ports = it },
                    label = { Text("Port Forwarding (Host:Container)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Memory limit
                OutlinedTextField(
                    value = ramMb,
                    onValueChange = { ramMb = it },
                    label = { Text("Memory Limit (MB)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (system.isInstalled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onOpenTerminal,
                            colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Shell", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = system.copy(
                        engineType = engine,
                        portMappings = ports,
                        neonSimdEnabled = simd,
                        memoryLimitMb = ramMb.toIntOrNull() ?: 1024,
                        isInstalled = true
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
            ) {
                Text(if (system.isInstalled) "Save Changes" else "Install & Start", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UDroidTextSecondary)
            }
        }
    )
}

@Composable
fun PullImageDialog(
    onDismiss: () -> Unit,
    onPull: (name: String, image: String, engine: String, ports: String, simd: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var imageRef by remember { mutableStateOf("docker.io/library/alpine:latest") }
    var engine by remember { mutableStateOf("Podman Rootless") }
    var ports by remember { mutableStateOf("8080:80") }
    var simd by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = UDroidGreen)
                Text("Pull Docker / Podman Image", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter any Docker Hub, GHCR, or Quay.io container image reference:",
                    fontSize = 12.sp,
                    color = UDroidTextSecondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Container Display Name") },
                    placeholder = { Text("e.g. My Web Server") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = imageRef,
                    onValueChange = { imageRef = it },
                    label = { Text("Image Reference") },
                    placeholder = { Text("e.g. nginx:alpine, redis:alpine") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ports,
                    onValueChange = { ports = it },
                    label = { Text("Port Mapping (Host:Container)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NEON SIMD Vector Acceleration", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = simd,
                        onCheckedChange = { simd = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = UDroidGreen
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (name.isNotBlank()) name else imageRef.substringAfterLast("/").substringBefore(":")
                    onPull(finalName, imageRef, engine, ports, simd)
                },
                enabled = imageRef.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
            ) {
                Text("Pull & Run", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UDroidTextSecondary)
            }
        }
    )
}
