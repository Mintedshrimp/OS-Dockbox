package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
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
    containerFlavor: String = "Debian",
    activeAppLaunch: String? = null,
    selectedWm: String = "XFCE4",
    onWmChange: (String) -> Unit = {},
    resolutionPreset: String = "1280x720 (HD)",
    onResolutionChange: (String) -> Unit = {},
    touchTrackpadMode: Boolean = true,
    onToggleTouchMode: () -> Unit = {}
) {
    val isWindows = remember(containerFlavor, activeDistro) {
        containerFlavor.equals("Windows", ignoreCase = true) ||
                activeDistro.contains("Windows", ignoreCase = true) ||
                activeDistro.contains("Wine", ignoreCase = true)
    }

    var activeWindow by remember(activeAppLaunch, isWindows) {
        mutableStateOf(
            activeAppLaunch ?: if (isWindows) "File Explorer" else "Chromium"
        )
    }

    var cursorPosition by remember { mutableStateOf(Offset(220f, 160f)) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Popup Keyboard State
    var showKeyboardBar by remember { mutableStateOf(false) }
    var typedBuffer by remember { mutableStateOf("") }
    var lastActionFeedback by remember { mutableStateOf<String?>(null) }

    // Gamepad Virtual Overlay State
    var showGamepadOverlay by remember { mutableStateOf(false) }
    var gamepadStatus by remember { mutableStateOf("Gamepad: Active & Listening") }

    // Hardware focus requester for capturing real gamepad/hardware key inputs
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        val nativeCode = keyEvent.nativeKeyEvent.keyCode
                        when (nativeCode) {
                            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> {
                                cursorPosition = Offset(cursorPosition.x, (cursorPosition.y - 20f).coerceAtLeast(0f))
                                gamepadStatus = "Gamepad: D-Pad UP (Y: ${cursorPosition.y.toInt()})"
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> {
                                cursorPosition = Offset(cursorPosition.x, (cursorPosition.y + 20f).coerceAtMost(800f))
                                gamepadStatus = "Gamepad: D-Pad DOWN (Y: ${cursorPosition.y.toInt()})"
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> {
                                cursorPosition = Offset((cursorPosition.x - 20f).coerceAtLeast(0f), cursorPosition.y)
                                gamepadStatus = "Gamepad: D-Pad LEFT (X: ${cursorPosition.x.toInt()})"
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> {
                                cursorPosition = Offset((cursorPosition.x + 20f).coerceAtMost(1200f), cursorPosition.y)
                                gamepadStatus = "Gamepad: D-Pad RIGHT (X: ${cursorPosition.x.toInt()})"
                                true
                            }
                            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER -> {
                                lastActionFeedback = "Left Click @ (${cursorPosition.x.toInt()}, ${cursorPosition.y.toInt()})"
                                gamepadStatus = "Gamepad: Button A (Left Click)"
                                true
                            }
                            KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BACK -> {
                                lastActionFeedback = "Right Click (Context Menu)"
                                gamepadStatus = "Gamepad: Button B (Right Click)"
                                true
                            }
                            KeyEvent.KEYCODE_BUTTON_X -> {
                                showKeyboardBar = !showKeyboardBar
                                gamepadStatus = "Gamepad: Button X (Toggle Keyboard)"
                                true
                            }
                            KeyEvent.KEYCODE_BUTTON_Y -> {
                                showGamepadOverlay = !showGamepadOverlay
                                gamepadStatus = "Gamepad: Button Y (Toggle Controls)"
                                true
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Desktop Top Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(if (isWindows) Color(0xFF0F2B48) else Color(0xFF1E293B))
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Distro Menu / Start Button + Window Taskbar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Start Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isWindows) Color(0xFF0078D4) else UDroidGreen,
                            modifier = Modifier.clickable { showSettingsMenu = !showSettingsMenu }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isWindows) Icons.Default.DesktopWindows else Icons.Default.Menu,
                                    contentDescription = "Start",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isWindows) "Start" else selectedWm,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Running Apps Tabs
                        val appTabs = if (isWindows) {
                            listOf("File Explorer", "Wine cmd", "Edge / Web", "Task Manager")
                        } else {
                            listOf("Chromium", "Terminal", "VS Code", "Files")
                        }

                        appTabs.forEach { win ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (activeWindow == win) (if (isWindows) Color(0xFF1E4976) else Color(0xFF334155)) else Color.Transparent)
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

                    // Right: Quick Controls (Touch/Trackpad, Keyboard, Gamepad, Mode Controls)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Mode Switcher (Touch vs Trackpad Mouse)
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

                        // Popup Keyboard Toggle Button
                        IconButton(
                            onClick = { showKeyboardBar = !showKeyboardBar },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Popup Keyboard",
                                tint = if (showKeyboardBar) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Gamepad Controls Toggle Button
                        IconButton(
                            onClick = { showGamepadOverlay = !showGamepadOverlay },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Gamepad Controls",
                                tint = if (showGamepadOverlay) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(17.dp)
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
                                Text(
                                    text = if (isWindows) "Shell Profile:" else "Window Manager:",
                                    fontSize = 11.sp,
                                    color = UDroidTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val options = if (isWindows) {
                                        listOf("Win11 ARM", "Wine/Proot", "Lite Explorer")
                                    } else {
                                        listOf("XFCE4", "LXQt", "Openbox")
                                    }
                                    options.forEach { wm ->
                                        FilterChip(
                                            selected = selectedWm == wm,
                                            onClick = { onWmChange(wm) },
                                            label = { Text(wm, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = if (isWindows) Color(0xFF0284C7) else UDroidGreen,
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
                        if (isWindows) {
                            DesktopIconItem(
                                icon = Icons.Default.Folder,
                                label = "This PC",
                                onClick = { activeWindow = "File Explorer" },
                                iconTint = Color(0xFF38BDF8)
                            )
                            DesktopIconItem(
                                icon = Icons.Default.Terminal,
                                label = "Command",
                                onClick = { activeWindow = "Wine cmd" },
                                iconTint = Color(0xFFE2E8F0)
                            )
                            DesktopIconItem(
                                icon = Icons.Default.Language,
                                label = "Edge",
                                onClick = { activeWindow = "Edge / Web" },
                                iconTint = Color(0xFF0EA5E9)
                            )
                            DesktopIconItem(
                                icon = Icons.Default.BarChart,
                                label = "TaskMgr",
                                onClick = { activeWindow = "Task Manager" },
                                iconTint = Color(0xFF10B981)
                            )
                        } else {
                            DesktopIconItem(icon = Icons.Default.Folder, label = "Files", onClick = { activeWindow = "Files" })
                            DesktopIconItem(icon = Icons.Default.Language, label = "Chromium", onClick = { activeWindow = "Chromium" })
                            DesktopIconItem(icon = Icons.Default.Code, label = "VS Code", onClick = { activeWindow = "VS Code" })
                            DesktopIconItem(icon = Icons.Default.Terminal, label = "UXTerm", onClick = { activeWindow = "Terminal" })
                        }
                    }

                    // Simulated Main Active Window
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 74.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isWindows) Color(0xFF1B2A3B) else Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, if (isWindows) Color(0xFF0078D4) else Color(0xFF475569))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Title bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isWindows) Color(0xFF0F1E2E) else Color(0xFF0F172A))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isWindows) {
                                        Icon(
                                            imageVector = Icons.Default.DesktopWindows,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isWindows) {
                                            when (activeWindow) {
                                                "File Explorer" -> "File Explorer - C:\\Users\\User\\Documents"
                                                "Wine cmd" -> "Wine Command Prompt [Version 9.0 ARM64]"
                                                "Edge / Web" -> "Microsoft Edge Browser - [$resolutionPreset]"
                                                else -> "Windows Task Manager - ARM64 Virtual Cores Active"
                                            }
                                        } else {
                                            when (activeWindow) {
                                                "Chromium" -> "Chromium Web Browser - [$activeDistro | $selectedWm]"
                                                "Terminal" -> "uxterm ($selectedWm @ $resolutionPreset)"
                                                "VS Code" -> "Visual Studio Code Server (Port 8080 :0)"
                                                else -> "Nautilus File Manager - /root"
                                            }
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
                                    .background(
                                        when {
                                            isWindows && activeWindow == "Wine cmd" -> Color(0xFF0C0C0C)
                                            activeWindow == "Terminal" -> Color(0xFF0B1114)
                                            else -> Color.White
                                        }
                                    )
                                    .padding(12.dp)
                            ) {
                                if (isWindows) {
                                    when (activeWindow) {
                                        "File Explorer" -> {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFF1F5F9))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Computer, contentDescription = null, tint = Color(0xFF0078D4), modifier = Modifier.size(16.dp))
                                                    Text("C:\\Windows\\System32", fontSize = 12.sp, color = UDroidTextPrimary, fontWeight = FontWeight.Medium)
                                                }
                                                Text(
                                                    text = "Windows ARM64 Container Environment (Wine 9.0 / QEMU)",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = UDroidTextPrimary
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                    WindowsDriveItem("Local Disk (C:)", "38.2 GB free / 64 GB")
                                                    WindowsDriveItem("Container SD (D:)", "112 GB free / 128 GB")
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("System Folders:", fontSize = 11.sp, color = UDroidTextSecondary, fontWeight = FontWeight.Bold)
                                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                    FolderItem("Program Files")
                                                    FolderItem("System32")
                                                    FolderItem("Users")
                                                    FolderItem("Games")
                                                }
                                            }
                                        }

                                        "Wine cmd" -> {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Microsoft Windows [Version 10.0.22621.1]", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFFCCCCCC))
                                                Text("(c) Microsoft Corporation. All rights reserved.", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF888888))
                                                Text("Wine ARM64/x86_64 WoW64 Translation Layer ACTIVE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF00E5FF))
                                                Text("C:\\Users\\User> dir C:\\", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.White)
                                                Text("  Directory of C:\\", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF888888))
                                                Text("  09/01/2026  12:00 AM    <DIR>          Program Files", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFCCCCCC))
                                                Text("  09/01/2026  12:00 AM    <DIR>          Windows", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFCCCCCC))
                                                Text("C:\\Users\\User> _", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF00E5FF))
                                            }
                                        }

                                        "Edge / Web" -> {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFF1F5F9))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF0078D4), modifier = Modifier.size(14.dp))
                                                    Text("https://www.google.com", fontSize = 12.sp, color = UDroidTextPrimary, fontWeight = FontWeight.Medium)
                                                }
                                                Text(
                                                    text = "Windows Browser Rendering Engine",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = UDroidTextPrimary
                                                )
                                                Text(
                                                    text = "Accelerated DXVK / Direct3D to Vulkan bridge enabled. Full hardware acceleration active through virtio-gpu driver.",
                                                    fontSize = 12.sp,
                                                    color = UDroidTextSecondary,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        else -> {
                                            // Task Manager
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Processes & Performance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
                                                TaskItemRow("explorer.exe", "0.4%", "42 MB")
                                                TaskItemRow("wine64-preloader", "1.2%", "118 MB")
                                                TaskItemRow("services.exe", "0.1%", "16 MB")
                                                TaskItemRow("winedevice.exe", "0.2%", "28 MB")
                                            }
                                        }
                                    }
                                } else {
                                    // Linux Window Apps
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

                    // Bottom Floating Left/Right Mouse Buttons (for precise touch mouse controls)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFF64748B)),
                            modifier = Modifier
                                .clickable {
                                    lastActionFeedback = "Left Click @ (${cursorPosition.x.toInt()}, ${cursorPosition.y.toInt()})"
                                }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = UDroidGreen, modifier = Modifier.size(14.dp))
                                Text("L-Click", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFF64748B)),
                            modifier = Modifier
                                .clickable {
                                    lastActionFeedback = "Right Click (Menu Opened)"
                                }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Text("R-Click", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Floating Virtual Gamepad Overlay (D-Pad, A/B/X/Y buttons)
                    AnimatedVisibility(
                        visible = showGamepadOverlay,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        VirtualGamepadCard(
                            onDirectionMove = { dx, dy ->
                                cursorPosition = Offset(
                                    x = (cursorPosition.x + dx).coerceIn(0f, 1200f),
                                    y = (cursorPosition.y + dy).coerceIn(0f, 800f)
                                )
                                gamepadStatus = "Gamepad: D-Pad Moved (${cursorPosition.x.toInt()}, ${cursorPosition.y.toInt()})"
                            },
                            onButtonA = {
                                lastActionFeedback = "Button A (Left Click)"
                                gamepadStatus = "Gamepad: Button A"
                            },
                            onButtonB = {
                                lastActionFeedback = "Button B (Right Click)"
                                gamepadStatus = "Gamepad: Button B"
                            },
                            onButtonX = {
                                showKeyboardBar = !showKeyboardBar
                                gamepadStatus = "Gamepad: Button X (Toggle Keyboard)"
                            },
                            onButtonY = {
                                lastActionFeedback = "Button Y (Menu/Escape)"
                                gamepadStatus = "Gamepad: Button Y"
                            },
                            onClose = { showGamepadOverlay = false }
                        )
                    }

                    // Status / Click feedback banner
                    if (lastActionFeedback != null || gamepadStatus.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Text(
                                    text = lastActionFeedback ?: gamepadStatus,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }

                // Popup Keyboard / Accessory Keys Bar
                AnimatedVisibility(visible = showKeyboardBar) {
                    PopupKeyboardBar(
                        typedBuffer = typedBuffer,
                        onTypedChange = { typedBuffer = it },
                        onSendKey = { key ->
                            lastActionFeedback = "Key: $key sent to container"
                            if (key == "Enter") {
                                typedBuffer = ""
                            }
                        },
                        onClose = { showKeyboardBar = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopIconItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color = Color.White
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
                Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(20.dp))
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

@Composable
private fun WindowsDriveItem(title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.width(150.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF0078D4), modifier = Modifier.size(24.dp))
            Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
                Text(subtitle, fontSize = 9.sp, color = UDroidTextMuted)
            }
        }
    }
}

@Composable
private fun TaskItemRow(name: String, cpu: String, mem: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
            Text(name, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = UDroidTextPrimary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("CPU: $cpu", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
            Text("RAM: $mem", fontSize = 11.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PopupKeyboardBar(
    typedBuffer: String,
    onTypedChange: (String) -> Unit,
    onSendKey: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            // Special function keys row (Ctrl, Alt, Tab, Esc, Super, Arrows)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val specialKeys = listOf("Esc", "Tab", "Ctrl", "Alt", "Win", "Enter", "Backspace", "Del", "↑", "↓", "←", "→", "F1", "F5", "F11")
                specialKeys.forEach { key ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (key) {
                            "Enter" -> Color(0xFF16A34A)
                            "Esc" -> Color(0xFFDC2626)
                            "Ctrl", "Alt", "Win" -> Color(0xFF1E3A8A)
                            else -> Color(0xFF1E293B)
                        },
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.clickable { onSendKey(key) }
                    ) {
                        Text(
                            text = key,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.KeyboardHide, contentDescription = "Hide Keyboard", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Direct interactive text injection bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = typedBuffer,
                            onValueChange = onTypedChange,
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(UDroidGreen),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (typedBuffer.isEmpty()) {
                                    Text(
                                        "Type text to send to GUI / wine session...",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (typedBuffer.isNotBlank()) {
                            onSendKey("Text: $typedBuffer")
                            onSendKey("Enter")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VirtualGamepadCard(
    onDirectionMove: (dx: Float, dy: Float) -> Unit,
    onButtonA: () -> Unit,
    onButtonB: () -> Unit,
    onButtonX: () -> Unit,
    onButtonY: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(
                modifier = Modifier.width(220.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Text("Gamepad Controls", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // D-Pad and Action Buttons side-by-side
            Row(
                modifier = Modifier.width(220.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // D-Pad
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // UP
                    GamepadMiniButton(label = "▲") { onDirectionMove(0f, -25f) }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        GamepadMiniButton(label = "◀") { onDirectionMove(-25f, 0f) }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("D", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        }
                        GamepadMiniButton(label = "▶") { onDirectionMove(25f, 0f) }
                    }
                    // DOWN
                    GamepadMiniButton(label = "▼") { onDirectionMove(0f, 25f) }
                }

                // A B X Y action cluster
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Y Button (Top)
                    GamepadActionButton(label = "Y", color = Color(0xFFF59E0B), onClick = onButtonY)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // X Button (Left)
                        GamepadActionButton(label = "X", color = Color(0xFF0284C7), onClick = onButtonX)
                        Spacer(modifier = Modifier.width(4.dp))
                        // B Button (Right)
                        GamepadActionButton(label = "B", color = Color(0xFFEF4444), onClick = onButtonB)
                    }
                    // A Button (Bottom)
                    GamepadActionButton(label = "A", color = Color(0xFF10B981), onClick = onButtonA)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Hardware gamepad keys (D-Pad, A, B, X, Y) active",
                fontSize = 9.sp,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun GamepadMiniButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF475569)),
        modifier = Modifier
            .size(24.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GamepadActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.25f),
        border = BorderStroke(1.2.dp, color),
        modifier = Modifier
            .size(26.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
