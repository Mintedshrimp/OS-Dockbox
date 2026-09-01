package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun DesktopViewerDialog(
    onDismiss: () -> Unit,
    activeDistro: String = "Debian 13 (Cinnamon Desktop)"
) {
    var activeWindow by remember { mutableStateOf("Chromium") } // "Chromium", "Terminal", "Files"
    var virGlEnabled by remember { mutableStateOf(true) }

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
                // Desktop Top Panel (Cinnamon/XFCE panel)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Distro Menu + Running Task Items
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Menu
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(UDroidGreen)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text("Menu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Window Taskbar buttons
                        listOf("Chromium", "Terminal", "Files").forEach { win ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (activeWindow == win) Color(0xFF334155) else Color.Transparent)
                                    .clickable { activeWindow = win }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
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

                    // Right: Clock & Exit
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "DISPLAY :0 • 60 FPS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF22C55E)
                        )

                        Text(
                            text = "12:43 PM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close desktop", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Desktop Wallpaper & Active Window Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    // Desktop Background Art & Icons
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DesktopIconItem(icon = Icons.Default.Folder, label = "Home", onClick = { activeWindow = "Files" })
                        DesktopIconItem(icon = Icons.Default.Language, label = "Chromium", onClick = { activeWindow = "Chromium" })
                        DesktopIconItem(icon = Icons.Default.Terminal, label = "UXTerm", onClick = { activeWindow = "Terminal" })
                        DesktopIconItem(icon = Icons.Default.Delete, label = "Trash", onClick = {})
                    }

                    // Floating Simulated Window
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 70.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Window Header Titlebar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
                                            "Chromium" -> "Chromium Web Browser - [aarch64 NEON Accelerated]"
                                            "Terminal" -> "uxterm (rootless podman container)"
                                            else -> "Files - /home/dockbox"
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

                            // Window Content
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (activeWindow == "Terminal") Color(0xFF0C1315) else Color.White)
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
                                                Text("https://podman.io", fontSize = 12.sp, color = UDroidTextPrimary, fontWeight = FontWeight.Medium)
                                            }
                                            Text(
                                                text = "Podman Rootless Container Hub",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                color = UDroidTextPrimary
                                            )
                                            Text(
                                                text = "Running Chromium via software rasterization + NEON SIMD vector pipeline. Hardware acceleration active with zero kernel root requirements.",
                                                fontSize = 12.sp,
                                                color = UDroidTextSecondary,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                    "Terminal" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("dockbox@podman-desktop:~$ neofetch", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF38BDF8))
                                            Text("OS: Debian 13 (Trixie) aarch64", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF22C55E))
                                            Text("Host: Qualcomm Snapdragon Kryo 680 (ARMv8.2-A)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White)
                                            Text("Display: Cinnamon on Xwayland (:0.0)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White)
                                            Text("Engine: Podman Rootless crun (1.2% CPU overhead)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFFBBF24))
                                        }
                                    }
                                    else -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text("/home/dockbox", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = UDroidTextPrimary)
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                FolderItem("Desktop")
                                                FolderItem("Downloads")
                                                FolderItem("Documents")
                                                FolderItem("containers")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopIconItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        Text(text = label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FolderItem(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Folder, contentDescription = name, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
        Text(text = name, fontSize = 11.sp, color = UDroidTextPrimary, fontWeight = FontWeight.Medium)
    }
}
