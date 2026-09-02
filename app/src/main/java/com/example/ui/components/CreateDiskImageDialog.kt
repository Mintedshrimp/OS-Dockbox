package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CreateDiskImageDialog(
    onDismiss: () -> Unit,
    onCreateDisk: (fileName: String, format: String, partitionType: String, sizeGb: Int, blockSize: String, prealloc: Boolean) -> Unit
) {
    var fileName by remember { mutableStateOf("linux-storage") }
    var selectedFormat by remember { mutableStateOf("QCOW2") }
    var selectedPartType by remember { mutableStateOf("EXT4") }
    var sizeGb by remember { mutableFloatStateOf(16f) }
    var selectedBlockSize by remember { mutableStateOf("4KB") }
    var preallocate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Blank Disk Image", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Disk Image Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Format Selector
                Text("Disk Format", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("QCOW2", "VHD", "VMDK", "RAW").forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt, fontSize = 11.sp) }
                        )
                    }
                }

                // Partition Type
                Text("Partition Filesystem", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("EXT4", "NTFS", "BTRFS", "FAT32", "XFS").forEach { pt ->
                        FilterChip(
                            selected = selectedPartType == pt,
                            onClick = { selectedPartType = pt },
                            label = { Text(pt, fontSize = 11.sp) }
                        )
                    }
                }

                // Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Disk Capacity", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${sizeGb.toInt()} GB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidGreen)
                }
                Slider(
                    value = sizeGb,
                    onValueChange = { sizeGb = it },
                    valueRange = 1f..128f,
                    steps = 127
                )

                // Block Size
                Text("Cluster / Block Size", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("512B", "4KB", "64KB", "1MB").forEach { bs ->
                        FilterChip(
                            selected = selectedBlockSize == bs,
                            onClick = { selectedBlockSize = bs },
                            label = { Text(bs, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = preallocate, onCheckedChange = { preallocate = it })
                    Text("Preallocate metadata (Sparse file)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        onCreateDisk(fileName, selectedFormat, selectedPartType, sizeGb.toInt(), selectedBlockSize, preallocate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen)
            ) {
                Text("Build Disk", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
