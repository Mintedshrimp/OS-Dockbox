package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ContainerSystemEntity
import com.example.ui.theme.*

@Composable
fun AddCustomAppDialog(
    installedContainers: List<ContainerSystemEntity>,
    selectedContainerId: String?,
    onDismiss: () -> Unit,
    onAddApp: (containerId: String, name: String, category: String, command: String, displayType: String, description: String, port: Int) -> Unit
) {
    var chosenContainerId by remember { mutableStateOf(selectedContainerId ?: installedContainers.firstOrNull()?.id ?: "debian-trixie") }
    var appName by remember { mutableStateOf("") }
    var appCategory by remember { mutableStateOf("Development") }
    var appCommand by remember { mutableStateOf("") }
    var displayType by remember { mutableStateOf("DISPLAY :0") }
    var appDescription by remember { mutableStateOf("") }
    var portNumber by remember { mutableStateOf("8080") }

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
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddBox, contentDescription = null, tint = UDroidGreen, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Add Container App", fontSize = 17.sp, fontWeight = FontWeight.Black, color = UDroidTextPrimary)
                            Text("Register desktop GUI or CLI launcher", fontSize = 12.sp, color = UDroidTextSecondary)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = UDroidTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Container Target Picker
                Text("Target Container:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    installedContainers.forEach { container ->
                        FilterChip(
                            selected = chosenContainerId == container.id,
                            onClick = { chosenContainerId = container.id },
                            label = { Text(container.name.take(15), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = UDroidGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // App Name
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("Application Name (e.g., GIMP, Jupyter, Ollama)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UDroidGreen,
                        unfocusedBorderColor = UDroidCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Command
                OutlinedTextField(
                    value = appCommand,
                    onValueChange = { appCommand = it },
                    label = { Text("Executable Launch Command", fontSize = 12.sp) },
                    placeholder = { Text("e.g., code-server --port 8080, gimp, htop", fontSize = 12.sp, color = UDroidTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UDroidGreen,
                        unfocusedBorderColor = UDroidCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category & Display Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Development", "Utility", "Web", "System").forEach { cat ->
                        FilterChip(
                            selected = appCategory == cat,
                            onClick = { appCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("DISPLAY :0", "CLI Terminal", "PORT 8080").forEach { dt ->
                        FilterChip(
                            selected = displayType == dt,
                            onClick = { displayType = dt },
                            label = { Text(dt, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF15803D),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = appDescription,
                    onValueChange = { appDescription = it },
                    label = { Text("Description (Optional)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UDroidGreen,
                        unfocusedBorderColor = UDroidCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val portInt = portNumber.toIntOrNull() ?: 0
                        onAddApp(
                            chosenContainerId,
                            appName.ifBlank { "Custom User App" },
                            appCategory,
                            appCommand.ifBlank { "bash" },
                            displayType,
                            appDescription.ifBlank { "User installed application" },
                            portInt
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UDroidGreen),
                    enabled = appName.isNotBlank()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Register & Install App", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
