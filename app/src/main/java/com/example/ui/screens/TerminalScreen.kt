package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.TerminalLine
import com.example.domain.model.TerminalLineType
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel
import com.example.ui.viewmodel.MainTab

@Composable
fun TerminalScreen(
    viewModel: ContainerViewModel,
    modifier: Modifier = Modifier
) {
    val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()
    val terminalInput by viewModel.terminalInput.collectAsStateWithLifecycle()
    val allSystems by viewModel.allSystems.collectAsStateWithLifecycle()
    val activeContainerId by viewModel.activeContainerId.collectAsStateWithLifecycle()

    val runningSystems = remember(allSystems) { allSystems.filter { it.isRunning } }
    val installedSystems = remember(allSystems) { allSystems.filter { it.isInstalled } }
    val activeSystem = remember(allSystems, activeContainerId) {
        allSystems.find { it.id == activeContainerId } ?: runningSystems.firstOrNull() ?: installedSystems.firstOrNull()
    }

    val listState = rememberLazyListState()
    var showContainerDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UDroidBg)
    ) {
        HeaderBar(
            title = "OS Dockbox",
            statusText = if (activeSystem?.isRunning == true) "SESSION LIVE" else "TERMINAL READY",
            version = "v0.1.1"
        )

        // Terminal Top Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Container Selector Pill
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .clickable { showContainerDropdown = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (activeSystem?.isRunning == true) Color(0xFF16A34A) else Color(0xFF94A3B8))
                    )
                    Text(
                        text = if (activeSystem != null) activeSystem.name else "No Container Selected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = UDroidTextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Switch container",
                        tint = UDroidTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showContainerDropdown,
                    onDismissRequest = { showContainerDropdown = false }
                ) {
                    if (installedSystems.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No containers installed yet") },
                            onClick = {
                                viewModel.selectTab(MainTab.OS)
                                showContainerDropdown = false
                            }
                        )
                    } else {
                        installedSystems.forEach { sys ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${sys.name} (${if (sys.isRunning) "Running" else "Stopped"})",
                                        fontWeight = if (sys.id == activeContainerId) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setActiveContainer(sys.id)
                                    showContainerDropdown = false
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (sys.isRunning) Color(0xFF16A34A) else Color(0xFF94A3B8))
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Quick Stats Tag (NEON / Fallback Active)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (activeSystem?.isFallbackEngaged == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "PROOT FALLBACK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0F2FE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (activeSystem?.isRunning == true) "TTY ATTACHED" else "STANDBY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1)
                    )
                }
            }
        }

        // Quick Command Shortcuts Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quickCmds = listOf(
                "podman ps",
                "proot-info",
                "switch-engine",
                "podman stats",
                "simd-bench",
                "neofetch",
                "apt update",
                "clear"
            )
            items(quickCmds) { cmd ->
                SuggestionChip(
                    onClick = { viewModel.executeTerminalCommand(cmd) },
                    label = { Text(cmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (cmd == "proot-info" || cmd == "switch-engine") Color(0xFFF0FDF4) else Color.White,
                        labelColor = if (cmd == "proot-info" || cmd == "switch-engine") UDroidGreenDark else UDroidTextPrimary
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = if (cmd == "proot-info" || cmd == "switch-engine") Color(0xFFBBF7D0) else UDroidCardBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Dark Terminal Console Window
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UDroidDarkSurface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Terminal Title bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(UDroidDarkCard)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeSystem != null) "root@${activeSystem.id}:~#" else "terminal@dockbox:~# (idle)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.executeTerminalCommand("clear") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear terminal",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Terminal Content Area
                if (terminalLines.isEmpty()) {
                    // Terminal is blank / waiting state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Terminal Standby",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = if (installedSystems.isEmpty()) {
                                    "No container OS installed yet. Use the OS tab search bar to find and install a container (Ubuntu, Debian, Alpine)."
                                } else {
                                    "Run an installed container or enter a command below to attach interactive rootfs shell."
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            if (installedSystems.isNotEmpty()) {
                                val firstInst = installedSystems.first()
                                OutlinedButton(
                                    onClick = {
                                        viewModel.toggleSystem(firstInst)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UDroidGreen)
                                ) {
                                    Text("Launch ${firstInst.name} Terminal", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Render Terminal Lines
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(terminalLines, key = { it.id }) { line ->
                            TerminalLineView(line = line)
                        }
                    }
                }
            }
        }

        // On-screen Accessory Keyboard Shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val keyShortcuts = listOf("Ctrl", "Alt", "Tab", "Esc", "↑", "↓", "|", "~", "/", "-", "podman", "help")
            keyShortcuts.forEach { key ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .clickable {
                            when (key) {
                                "Tab" -> viewModel.setTerminalInput(terminalInput + " ")
                                "podman" -> viewModel.setTerminalInput("podman ")
                                "help" -> viewModel.executeTerminalCommand("help")
                                else -> viewModel.setTerminalInput(terminalInput + key)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = key,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = UDroidTextPrimary
                    )
                }
            }
        }

        // Terminal Interactive Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = terminalInput,
                onValueChange = { viewModel.setTerminalInput(it) },
                placeholder = {
                    Text(
                        text = if (activeSystem?.isRunning == true) "root@${activeSystem.id}:~# command..." else "Type command (e.g., podman, neofetch)...",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = UDroidTextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = UDroidGreen,
                    unfocusedBorderColor = UDroidCardBorder
                ),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = UDroidTextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    viewModel.executeTerminalCommand(terminalInput)
                })
            )

            IconButton(
                onClick = { viewModel.executeTerminalCommand(terminalInput) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UDroidGreen)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute Command",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TerminalLineView(line: TerminalLine) {
    val color = when (line.type) {
        TerminalLineType.INPUT -> UDroidTerminalCyan
        TerminalLineType.HEADER -> UDroidTerminalYellow
        TerminalLineType.SUCCESS -> UDroidTerminalGreen
        TerminalLineType.ERROR -> UDroidTerminalRed
        TerminalLineType.WARNING -> UDroidTerminalYellow
        TerminalLineType.OUTPUT -> Color(0xFFE2E8F0)
    }

    Text(
        text = line.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = color
    )
}
