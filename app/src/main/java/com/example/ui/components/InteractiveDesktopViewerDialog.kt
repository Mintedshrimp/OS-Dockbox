package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.DesktopWindowMode

@Composable
fun InteractiveDesktopViewerDialog(
    onDismiss: () -> Unit,
    onSwitchMode: (DesktopWindowMode) -> Unit = {},
    onTriggerSystemPip: () -> Unit = {},
    activeDistro: String = "Debian 13 (Trixie)",
    activeAppLaunch: String? = null,
    selectedWm: String = "XFCE4",
    onWmChange: (String) -> Unit = {},
    resolutionPreset: String = "1280x720 (HD)",
    onResolutionChange: (String) -> Unit = {},
    touchTrackpadMode: Boolean = true,
    onToggleTouchMode: () -> Unit = {}
) {
    var activeWindow by remember(activeAppLaunch) { mutableStateOf(activeAppLaunch ?: "Chromium") }
    var cursorPosition by remember { mutableStateOf(Offset(220f, 160f)) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Desktop Top Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Distro Menu + Window Taskbar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // WM Badge / Start
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = UDroidGreen,
                            modifier = Modifier.clickable { showSettingsMenu = !showSettingsMenu }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Text(selectedWm, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Running Apps Tabs
                        listOf("Chromium", "Terminal", "VS Code", "Files").forEach { win ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (activeWindow == win) Color(0xFF334155) else Color.Transparent)
                                    .clickable { activeWindow = win }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = win,
                                    fontSize = 11.sp,
                                    fontWeight = if (activeWindow == win) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeWindow == win) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    // Right: Diagnostic Info, Trackpad Toggle & Window Mode Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (touchTrackpadMode) Color(0xFF065F46) else Color(0xFF1E3A8A),
                            modifier = Modifier.clickable { onToggleTouchMode() }
                        ) {
                            Text(
                                text = if (touchTrackpadMode) "👆 Touch" else "🖱️ Trackpad",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        // In-App PiP Mode Button
                        IconButton(
                            onClick = { onSwitchMode(DesktopWindowMode.PIP) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.PictureInPicture, contentDescription = "Mini PiP Mode", tint = Color(0xFFE2E8F0), modifier = Modifier.size(16.dp))
                        }

                        // Free-form Floating Window Button
                        IconButton(
                            onClick = { onSwitchMode(DesktopWindowMode.FREEFORM) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.FitScreen, contentDescription = "Freeform Window", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        }

                        // System Native Phone PiP Button
                        IconButton(
                            onClick = onTriggerSystemPip,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Phone PiP Mode", tint = UDroidGreen, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Sub-Toolbar Settings (WM switcher & Resolution selector)
                AnimatedVisibility(visible = showSettingsMenu) {
                    Surface(
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Window Manager:", fontSize = 11.sp, color = UDroidTextSecondary, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("XFCE4", "LXQt", "Openbox").forEach { wm ->
                                        FilterChip(
                                            selected = selectedWm == wm,
                                            onClick = { onWmChange(wm) },
                                            label = { Text(wm, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = UDroidGreen,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Resolution Preset:", fontSize = 11.sp, color = UDroidTextSecondary, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("1280x720 (HD)", "1920x1080 (FHD)", "Auto Native").forEach { res ->
                                        FilterChip(
                                            selected = resolutionPreset == res,
                                            onClick = { onResolutionChange(res) },
                                            label = { Text(res, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF0284C7),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Desktop Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
                        .pointerInput(touchTrackpadMode) {
                            if (!touchTrackpadMode) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    cursorPosition = Offset(
                                        x = (cursorPosition.x + dragAmount.x).coerceIn(0f, 1200f),
                                        y = (cursorPosition.y + dragAmount.y).coerceIn(0f, 800f)
                                    )
                                }
                            }
                        }
                ) {
                    // Left App Icons
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DesktopIconItem(icon = Icons.Default.Folder, label = "Files", onClick = { activeWindow = "Files" })
                        DesktopIconItem(icon = Icons.Default.Language, label = "Chromium", onClick = { activeWindow = "Chromium" })
                        DesktopIconItem(icon = Icons.Default.Code, label = "VS Code", onClick = { activeWindow = "VS Code" })
                        DesktopIconItem(icon = Icons.Default.Terminal, label = "UXTerm", onClick = { activeWindow = "Terminal" })
                    }

                    // Simulated Main Active Window
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 74.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Title bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (activeWindow) {
                                            "Chromium" -> "Chromium Web Browser - [$activeDistro | $selectedWm]"
                                            "Terminal" -> "uxterm ($selectedWm @ $resolutionPreset)"
                                            "VS Code" -> "Visual Studio Code Server (Port 8080 :0)"
                                            else -> "Nautilus File Manager - /root"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Minimize, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                    Icon(Icons.Default.CropSquare, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                }
                            }

                            // Window Body
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (activeWindow == "Terminal") Color(0xFF0B1114) else Color.White)
                                    .padding(12.dp)
                            ) {
                                when (activeWindow) {
                                    "Chromium" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF1F5F9))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                Text("https://registry.hub.docker.com", fontSize = 12.sp, color = UDroidTextPrimary, fontWeight = FontWeight.Medium)
                                            }
                                            Text(
                                                text = "Accelerated Container X11 / Wayland Session",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                color = UDroidTextPrimary
                                            )
                                            Text(
                                                text = "Displaying interactive rasterized surface on DISPLAY :0 with $selectedWm window management. ARM NEON SIMD acceleration enabled for high-FPS rendering.",
                                                fontSize = 12.sp,
                                                color = UDroidTextSecondary,
                                                lineHeight = 16.sp
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { onSwitchMode(DesktopWindowMode.FREEFORM) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
                                                ) {
                                                    Icon(Icons.Default.FitScreen, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Switch to Freeform Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = onTriggerSystemPip
                                                ) {
                                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Phone PiP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    "Terminal" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Debian GNU/Linux 13 (trixie) [DISPLAY :0.0]", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF22C55E))
                                            Text("Linux 6.6.0-arm64-v8a aarch64", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            Text("root@dockbox:~# virglrenderer --version", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.White)
                                            Text("virglrenderer 1.0.0 (3D OpenGL ES acceleration ACTIVE)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF38BDF8))
                                            Text("root@dockbox:~# _", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF22C55E))
                                        }
                                    }

                                    "VS Code" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("📁 dockbox-workspace > app.py", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
                                            Surface(
                                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                                color = Color(0xFF1E1E1E),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text("1  from flask import Flask", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF9CDCFE))
                                                    Text("2  app = Flask(__name__)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFDCDCAA))
                                                    Text("3  @app.route('/')", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFDCDCAA))
                                                    Text("4  def hello(): return 'Live Linux Server!'", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFCE9178))
                                                }
                                            }
                                        }
                                    }

                                    else -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("File Manager (Nautilus) - /home/root", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                FolderItem("bin")
                                                FolderItem("etc")
                                                FolderItem("home")
                                                FolderItem("opt")
                                                FolderItem("var")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating Mouse Cursor in Trackpad Mode
                    if (!touchTrackpadMode) {
                        Box(
                            modifier = Modifier
                                .offset(x = cursorPosition.x.dp, y = cursorPosition.y.dp)
                                .size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Mouse Cursor",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopIconItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF334155),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FolderItem(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(28.dp))
        Text(name, fontSize = 11.sp, color = UDroidTextPrimary)
    }
}
