package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun RootInstallerDialog(
    onDismiss: () -> Unit,
    onInstallRootfs: (name: String, fileName: String, sizeMb: Long, engine: String) -> Unit
) {
    var selectedMiniOsPreset by remember { mutableStateOf("TinyCore Linux (CorePlus)") }
    var customName by remember { mutableStateOf("TinyCore Linux 15.0") }
    var selectedTarball by remember { mutableStateOf("tinycore-arm64-rootfs.tar.gz") }
    var selectedEngine by remember { mutableStateOf("PRoot-Distro") } // PRoot-Distro vs Podman Rootless
    var selectedSizeMb by remember { mutableLongStateOf(16L) }

    val presets = listOf(
        MiniOsPreset("TinyCore Linux (CorePlus)", "tinycore-arm64-rootfs.tar.gz", 16L, "PRoot-Distro", "16 MB micro GUI with FLWM", "FLWM GUI"),
        MiniOsPreset("Alpine Micro (Musl)", "alpine-minirootfs-3.22.tar.gz", 7L, "Podman Rootless", "7 MB lightning-fast minimal Linux", "CLI Ash"),
        MiniOsPreset("postmarketOS Edge", "pmos-edge-rootfs.tar.gz", 45L, "Podman Rootless", "True mobile Linux userland with Phosh", "Phosh UI"),
        MiniOsPreset("BusyBox Standalone", "busybox-rootfs-1.36.tar.gz", 3L, "PRoot-Distro", "3 MB single-binary POSIX userland", "Micro Ash"),
        MiniOsPreset("Custom SAF Archive (.tar.gz)", "custom-user-rootfs.tar.gz", 120L, "PRoot-Distro", "Import local rootfs archive from storage", "Custom")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, UDroidCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Rootfs Mini OS Installer", fontSize = 17.sp, fontWeight = FontWeight.Black, color = UDroidTextPrimary)
                            Text("Provision micro rootfs (.tar.gz, .img, .qcow2)", fontSize = 12.sp, color = UDroidTextSecondary)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = UDroidTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Text("Select Mini OS / Micro Rootfs:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                presets.forEach { preset ->
                    val isSelected = selectedMiniOsPreset == preset.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                selectedMiniOsPreset = preset.name
                                customName = preset.name
                                selectedTarball = preset.fileName
                                selectedSizeMb = preset.sizeMb
                                selectedEngine = preset.recommendedEngine
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF5F2) else UDroidCardSurface
                        ),
                        border = BorderStroke(1.dp, if (isSelected) UDroidGreen else UDroidCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(preset.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = UDroidTextPrimary)
                                Text("${preset.description} • ${preset.sizeMb} MB", fontSize = 11.sp, color = UDroidTextSecondary)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) UDroidGreen else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = preset.recommendedEngine,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else UDroidTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name field
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Container Instance Name", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UDroidGreen,
                        unfocusedBorderColor = UDroidCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Engine Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Execution Engine:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PRoot-Distro", "Podman Rootless").forEach { eng ->
                            FilterChip(
                                selected = selectedEngine == eng,
                                onClick = { selectedEngine = eng },
                                label = { Text(eng, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (eng.startsWith("Podman")) UDroidGreen else Color(0xFF0284C7),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Install Button
                Button(
                    onClick = {
                        onInstallRootfs(
                            customName.ifBlank { "Mini OS Container" },
                            selectedTarball,
                            selectedSizeMb,
                            selectedEngine
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
                ) {
                    Icon(Icons.Default.DownloadForOffline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unpack & Install Mini OS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class MiniOsPreset(
    val name: String,
    val fileName: String,
    val sizeMb: Long,
    val recommendedEngine: String,
    val description: String,
    val uiType: String
)
