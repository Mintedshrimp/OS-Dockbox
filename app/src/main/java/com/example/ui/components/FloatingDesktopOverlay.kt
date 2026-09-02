package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel
import com.example.ui.viewmodel.DesktopWindowMode
import kotlin.math.roundToInt

@Composable
fun FloatingDesktopOverlay(
    viewModel: ContainerViewModel,
    onEnterSystemPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowMode by viewModel.desktopWindowMode.collectAsState()
    val activeApp by viewModel.desktopActiveWindow.collectAsState()
    val selectedWm by viewModel.desktopSelectedWm.collectAsState()
    val resolution by viewModel.desktopResolution.collectAsState()
    val touchMode by viewModel.desktopTouchMode.collectAsState()
    val allSystems by viewModel.allSystems.collectAsState()
    val activeContainerId by viewModel.activeContainerId.collectAsState()

    val activeDistroName = allSystems.find { it.id == activeContainerId }?.name ?: "Debian 13 (Trixie)"

    if (windowMode == DesktopWindowMode.HIDDEN) return

    // Position state for Freeform and PiP
    var freeformOffset by remember { mutableStateOf(Offset(32f, 96f)) }
    var freeformWidth by remember { mutableStateOf(340.dp) }
    var freeformHeight by remember { mutableStateOf(260.dp) }
    var windowOpacity by remember { mutableFloatStateOf(0.96f) }

    var pipOffset by remember { mutableStateOf(Offset(20f, 120f)) }

    when (windowMode) {
        DesktopWindowMode.FULLSCREEN -> {
            InteractiveDesktopViewerDialog(
                onDismiss = { viewModel.setDesktopWindowMode(DesktopWindowMode.HIDDEN) },
                onSwitchMode = { mode -> viewModel.setDesktopWindowMode(mode) },
                onTriggerSystemPip = onEnterSystemPip,
                activeDistro = activeDistroName,
                activeAppLaunch = activeApp,
                selectedWm = selectedWm,
                onWmChange = { viewModel.setDesktopSelectedWm(it) },
                resolutionPreset = resolution,
                onResolutionChange = { viewModel.setDesktopResolution(it) },
                touchTrackpadMode = touchMode,
                onToggleTouchMode = { viewModel.toggleDesktopTouchMode() }
            )
        }

        DesktopWindowMode.FREEFORM -> {
            // Freeform Floating Window
            Box(
                modifier = modifier
                    .fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier
                        .offset { IntOffset(freeformOffset.x.roundToInt(), freeformOffset.y.roundToInt()) }
                        .size(width = freeformWidth, height = freeformHeight)
                        .alpha(windowOpacity)
                        .shadow(16.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.5.dp, UDroidGreen.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Draggable Title Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(Color(0xFF1E293B))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        freeformOffset = Offset(
                                            x = (freeformOffset.x + dragAmount.x).coerceAtLeast(0f),
                                            y = (freeformOffset.y + dragAmount.y).coerceAtLeast(0f)
                                        )
                                    }
                                }
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Distro Tag & Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Text(
                                    text = "$activeDistroName · $activeApp",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }

                            // Window Mode Buttons
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Mini PiP
                                IconButton(
                                    onClick = { viewModel.setDesktopWindowMode(DesktopWindowMode.PIP) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPicture,
                                        contentDescription = "Mini PiP",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Phone OS PiP
                                IconButton(
                                    onClick = onEnterSystemPip,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = "Phone PiP",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Maximize
                                IconButton(
                                    onClick = { viewModel.setDesktopWindowMode(DesktopWindowMode.FULLSCREEN) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Close
                                IconButton(
                                    onClick = { viewModel.setDesktopWindowMode(DesktopWindowMode.HIDDEN) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // App Task Switcher Ribbon
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Chromium", "Terminal", "VS Code", "Files").forEach { app ->
                                val isCurrent = activeApp == app
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isCurrent) UDroidGreen.copy(alpha = 0.2f) else Color.Transparent)
                                        .clickable { viewModel.setDesktopActiveWindow(app) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = app,
                                        fontSize = 10.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) UDroidGreen else Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = ":0 60fps",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF10B981)
                            )
                        }

                        // Window Body: Interactive Desktop View
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFF020617))
                        ) {
                            DesktopContentRenderer(
                                activeApp = activeApp,
                                activeDistro = activeDistroName,
                                selectedWm = selectedWm,
                                isCompact = true,
                                onLaunchApp = { viewModel.setDesktopActiveWindow(it) }
                            )

                            // Resize Handle at Bottom-Right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val newW = (freeformWidth.value + dragAmount.x / 2.5f).coerceIn(240f, 420f)
                                            val newH = (freeformHeight.value + dragAmount.y / 2.5f).coerceIn(180f, 480f)
                                            freeformWidth = newW.dp
                                            freeformHeight = newH.dp
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = "Resize",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        DesktopWindowMode.PIP -> {
            // In-App Compact Picture-in-Picture Card
            Box(
                modifier = modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier
                        .offset { IntOffset(pipOffset.x.roundToInt(), pipOffset.y.roundToInt()) }
                        .width(220.dp)
                        .height(140.dp)
                        .shadow(12.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.5.dp, UDroidGreen)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // PiP Header (Draggable)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(Color(0xFF1E293B))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        pipOffset = Offset(
                                            x = (pipOffset.x + dragAmount.x).coerceAtLeast(0f),
                                            y = (pipOffset.y + dragAmount.y).coerceAtLeast(0f)
                                        )
                                    }
                                }
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                                Text(
                                    text = "PiP · $activeApp",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                // Expand to Freeform
                                IconButton(
                                    onClick = { viewModel.setDesktopWindowMode(DesktopWindowMode.FREEFORM) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitScreen,
                                        contentDescription = "Freeform",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                // Expand to Fullscreen
                                IconButton(
                                    onClick = { viewModel.setDesktopWindowMode(DesktopWindowMode.FULLSCREEN) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                // Close PiP
                                IconButton(
                                    onClick = { viewModel.setDesktopWindowMode(DesktopWindowMode.HIDDEN) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // PiP Live Content Thumbnail (clickable to expand)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF020617))
                                .clickable { viewModel.setDesktopWindowMode(DesktopWindowMode.FREEFORM) }
                        ) {
                            DesktopContentRenderer(
                                activeApp = activeApp,
                                activeDistro = activeDistroName,
                                selectedWm = selectedWm,
                                isCompact = true,
                                onLaunchApp = {}
                            )

                            // Overlay tap hint
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tap to expand Freeform",
                                    fontSize = 9.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }

        DesktopWindowMode.HIDDEN -> {
            // Hidden
        }
    }
}

@Composable
fun DesktopContentRenderer(
    activeApp: String,
    activeDistro: String,
    selectedWm: String,
    isCompact: Boolean = false,
    onLaunchApp: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B111A))
    ) {
        when (activeApp) {
            "Chromium" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isCompact) 6.dp else 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Browser URL bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(10.dp))
                        Text(
                            text = "https://hub.docker.com",
                            fontSize = if (isCompact) 9.sp else 11.sp,
                            color = Color(0xFFCBD5E1),
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isCompact) 6.dp else 12.dp)
                        ) {
                            Text(
                                text = "Docker Hub Container Registry",
                                fontSize = if (isCompact) 11.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Running accelerated inside $activeDistro ($selectedWm) on DISPLAY :0",
                                fontSize = if (isCompact) 9.sp else 11.sp,
                                color = UDroidGreen
                            )
                            if (!isCompact) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "All web sessions and WebGL graphics render with zero-copy shared memory directly to Android surface.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            "Terminal" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF030712))
                        .padding(if (isCompact) 6.dp else 10.dp)
                ) {
                    Text(
                        text = "root@dockbox:~# uname -a",
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        color = Color(0xFF22C55E)
                    )
                    Text(
                        text = "Linux dockbox 6.6.0-arm64-v8a #1 SMP PREEMPT GNU/Linux",
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (isCompact) 8.sp else 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "root@dockbox:~# xhost +local:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        color = Color(0xFF22C55E)
                    )
                    Text(
                        text = "non-network local connections being added to access control list",
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (isCompact) 8.sp else 10.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "root@dockbox:~# _",
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        color = Color(0xFF22C55E)
                    )
                }
            }

            "VS Code" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF181824))
                        .padding(if (isCompact) 6.dp else 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("main.py — code-server", fontSize = if (isCompact) 9.sp else 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        Text("UTF-8 · Python 3.12", fontSize = if (isCompact) 8.sp else 10.sp, color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "import os, sys\nimport torch\n\nprint(f'Device: {torch.cuda.is_available()}')",
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (isCompact) 8.sp else 11.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            else -> {
                // Files / Generic App
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isCompact) 6.dp else 10.dp)
                ) {
                    Text(
                        text = "$activeApp — /root",
                        fontSize = if (isCompact) 10.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(if (isCompact) 16.dp else 24.dp))
                        Column {
                            Text("Documents/", fontSize = if (isCompact) 9.sp else 11.sp, color = Color.White)
                            Text("3 items · 4.2 MB", fontSize = if (isCompact) 8.sp else 9.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}
