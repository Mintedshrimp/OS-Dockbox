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
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel

@Composable
fun AppsScreen(
    viewModel: ContainerViewModel,
    modifier: Modifier = Modifier
) {
    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQueryApps.collectAsStateWithLifecycle()
    val activeContainerId by viewModel.activeContainerId.collectAsStateWithLifecycle()

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.displayType.contains(searchQuery, ignoreCase = true)
        }
    }

    val runningCount = allApps.count { it.isRunning }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UDroidBg),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            HeaderBar(
                title = "OS Dockbox",
                statusText = "SESSION LIVE",
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
                        text = "Linux apps",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = UDroidTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${allApps.size} apps • $activeContainerId • 232 ms",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UDroidTextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.showDesktop(true) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DesktopWindows,
                            contentDescription = "Open Graphical Desktop",
                            tint = UDroidGreen
                        )
                    }

                    IconButton(
                        onClick = { /* Refresh app registry */ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = UDroidTextSecondary
                        )
                    }
                }
            }
        }

        // Active Status Banner (matches screenshot)
        item {
            val runningApp = allApps.firstOrNull { it.isRunning }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    )
                    Text(
                        text = if (runningApp != null) "Running ${runningApp.name} on DISPLAY :0" else "Display server active • DISPLAY :0 ready",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
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
                        text = "Search installed apps",
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
                        IconButton(onClick = { viewModel.setSearchQueryApps("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = UDroidTextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
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

        // List of Apps
        items(filteredApps, key = { it.id }) { app ->
            AppItemCard(
                app = app,
                onToggle = { viewModel.toggleApp(app) },
                onLaunchDesktop = { viewModel.showDesktop(true) }
            )
        }
    }
}

@Composable
fun AppItemCard(
    app: ContainerAppEntity,
    onToggle: () -> Unit,
    onLaunchDesktop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
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
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(getAppIconBg(app.category)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAppIconVector(app.name, app.category),
                        contentDescription = app.name,
                        tint = getAppIconTint(app.category),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = app.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = UDroidTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = getAppSubtitle(app.name),
                        fontSize = 12.sp,
                        color = UDroidTextSecondary,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DesktopWindows,
                            contentDescription = null,
                            tint = UDroidTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = app.displayType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UDroidTextMuted
                        )
                    }
                }
            }

            // Launch & External Open Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onLaunchDesktop,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open in display",
                        tint = UDroidGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (app.isRunning) Color(0xFF14532D) else UDroidGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (app.isRunning) "Running" else "Launch",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getAppSubtitle(name: String): String {
    return when {
        name.contains("Network") -> "Manage and change your network connection..."
        name.contains("Chromium") -> "Web Browser"
        name.contains("Files") -> "Access and organize files"
        name.contains("Print") -> "Print Settings"
        name.contains("UXTerm") -> "xterm wrapper for Unicode environments"
        name.contains("XTerm") -> "standard terminal emulator for the X window..."
        name.contains("VS Code") -> "Web IDE development workspace"
        name.contains("HTOP") -> "Real-time process & resource viewer"
        else -> "Containerized system utility"
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
        name.contains("Files") -> Icons.Default.Folder
        name.contains("Print") -> Icons.Default.Print
        name.contains("Term") -> Icons.Default.Terminal
        name.contains("VS Code") -> Icons.Default.Code
        name.contains("HTOP") -> Icons.Default.Equalizer
        else -> Icons.Default.Widgets
    }
}
