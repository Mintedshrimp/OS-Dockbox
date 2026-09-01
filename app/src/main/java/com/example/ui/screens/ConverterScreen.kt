package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ConversionJobEntity
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel

@Composable
fun ConverterScreen(
    viewModel: ContainerViewModel,
    modifier: Modifier = Modifier
) {
    val conversions by viewModel.conversions.collectAsStateWithLifecycle()
    val showDialog by viewModel.showConvertDialog.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UDroidBg),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            HeaderBar(
                title = "OS Dockbox",
                statusText = "PODMAN READY",
                version = "v0.1.1"
            )
        }

        // Title & Description
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Text(
                    text = "Image Converter",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = UDroidTextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Convert ISO, VMDK & VHD images into Podman & Docker containers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UDroidTextSecondary
                )
            }
        }

        // Architecture & SIMD Speedup Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Transform,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Automated OCI Layer Packer",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UDroidTextPrimary
                                )
                                Text(
                                    text = "Extracts rootfs • Strips kernel • Packs OCI tarball",
                                    fontSize = 12.sp,
                                    color = UDroidTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.setShowConvertDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Job", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ConverterStatBadge("Source Formats", "ISO, VMDK, VHD, QCOW2")
                        ConverterStatBadge("SIMD Rate", "3.8x (NEON 128-bit)")
                        ConverterStatBadge("Output Spec", "OCI Image v1.1.0")
                    }
                }
            }
        }

        // Section: Conversion Pipeline Jobs
        item {
            Text(
                text = "CONVERSION JOBS & REPOSITORIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = UDroidTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, top = 14.dp, bottom = 6.dp)
            )
        }

        if (conversions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
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
                        Icon(Icons.Default.LayersClear, contentDescription = null, tint = UDroidTextMuted, modifier = Modifier.size(40.dp))
                        Text("No active conversion jobs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = UDroidTextPrimary)
                        Text("Tap 'New Job' to convert an ISO or VMDK image into a container.", fontSize = 13.sp, color = UDroidTextSecondary)
                    }
                }
            }
        } else {
            items(conversions, key = { it.id }) { job ->
                ConversionJobCard(job = job)
            }
        }
    }

    if (showDialog) {
        NewConversionDialog(
            onDismiss = { viewModel.setShowConvertDialog(false) },
            onStart = { source, format, target, reg, simd ->
                viewModel.startConversion(source, format, target, reg, simd)
            }
        )
    }
}

@Composable
fun ConversionJobCard(job: ConversionJobEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (job.status == "COMPLETED") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (job.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.Sync,
                            contentDescription = null,
                            tint = if (job.status == "COMPLETED") Color(0xFF15803D) else Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${job.targetImageName}:${job.targetTag}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = UDroidTextPrimary
                        )
                        Text(
                            text = "From: ${job.sourceFileName} (${job.sourceFormat})",
                            fontSize = 12.sp,
                            color = UDroidTextSecondary
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (job.status == "COMPLETED") Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = job.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (job.status == "COMPLETED") Color(0xFF15803D) else Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            if (job.progress < 1.0f) {
                LinearProgressIndicator(
                    progress = { job.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    color = UDroidGreen,
                    trackColor = Color(0xFFE2E9E5)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Stats breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Size: ${job.originalSizeMb}MB -> ${job.compressedSizeMb}MB (${((1 - job.compressedSizeMb/job.originalSizeMb) * 100).toInt()}% saved)",
                    fontSize = 11.sp,
                    color = UDroidTextSecondary
                )
                Text(
                    text = "NEON SIMD: ${job.neonSimdSpeedup}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )
            }

            if (job.logOutput.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(UDroidDarkSurface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = job.logOutput,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ConverterStatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = UDroidTextMuted, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
    }
}

@Composable
fun NewConversionDialog(
    onDismiss: () -> Unit,
    onStart: (source: String, format: String, target: String, reg: String, simd: Boolean) -> Unit
) {
    var sourceFile by remember { mutableStateOf("ubuntu-24.04-minimal.iso") }
    var sourceFormat by remember { mutableStateOf("ISO") }
    var targetName by remember { mutableStateOf("ubuntu-oci-stripped") }
    var registry by remember { mutableStateOf("docker.io/library") }
    var useSimd by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Transform, contentDescription = null, tint = UDroidGreen)
                Text("Convert ISO / VMDK to OCI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select source virtual disk/iso format and specify container output details:",
                    fontSize = 12.sp,
                    color = UDroidTextSecondary
                )

                // Format Selector Chips
                Text("Source Format:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ISO", "VMDK", "VHD", "QCOW2").forEach { fmt ->
                        FilterChip(
                            selected = sourceFormat == fmt,
                            onClick = {
                                sourceFormat = fmt
                                sourceFile = when (fmt) {
                                    "ISO" -> "distro-install.iso"
                                    "VMDK" -> "virtual-disk.vmdk"
                                    "VHD" -> "image-drive.vhd"
                                    else -> "disk.qcow2"
                                }
                            },
                            label = { Text(fmt, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCFCE7),
                                selectedLabelColor = UDroidGreen
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = sourceFile,
                    onValueChange = { sourceFile = it },
                    label = { Text("Source Image Path / URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetName,
                    onValueChange = { targetName = it },
                    label = { Text("Output OCI Image Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = registry,
                    onValueChange = { registry = it },
                    label = { Text("Target Container Registry") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hardware NEON SIMD Fast-Path", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = useSimd,
                        onCheckedChange = { useSimd = it },
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
                    onStart(sourceFile, sourceFormat, targetName, registry, useSimd)
                },
                colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
            ) {
                Text("Start Extraction & Conversion", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UDroidTextSecondary)
            }
        }
    )
}
