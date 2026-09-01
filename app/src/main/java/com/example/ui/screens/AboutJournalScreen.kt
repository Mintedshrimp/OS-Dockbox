package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.SupervisorLogEntity
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel

@Composable
fun AboutJournalScreen(
    viewModel: ContainerViewModel,
    modifier: Modifier = Modifier
) {
    val supervisorLogs by viewModel.supervisorLogs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var checkUpdateState by remember { mutableStateOf("Check") }

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

        // Title and Subtitle
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Text(
                    text = "About OS Dockbox",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = UDroidTextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "App details, updates, and diagnostics",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UDroidTextSecondary
                )
            }
        }

        // About Description Card (matches screenshot)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
                border = BorderStroke(1.dp, UDroidCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_osdockbox_logo),
                                contentDescription = "OS Dockbox",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = "OS Dockbox",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = UDroidTextPrimary
                            )
                        }

                        // Version badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A))
                                )
                                Text(
                                    text = "v0.1.1",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "A Linux experience shaped for Android",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = UDroidTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "The goal is a self–contained way to install and use Linux systems, while keeping the terminal close when it is useful. Powered by rootless Podman OCI containers and NEON SIMD vector optimization.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = UDroidTextSecondary
                    )
                }
            }
        }

        // Section: PROJECT
        item {
            Text(
                text = "PROJECT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = UDroidTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, top = 14.dp, bottom = 6.dp)
            )
        }

        // GitHub Repository Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clickable { /* external url action */ },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
                border = BorderStroke(1.dp, UDroidCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEFF5F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = UDroidTextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("GitHub repository", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
                            Text("Source code, releases, and project history", fontSize = 12.sp, color = UDroidTextSecondary)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = UDroidTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Support OS Dockbox Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
                border = BorderStroke(1.dp, UDroidCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEFF5F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = UDroidGreen, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Support OS Dockbox", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
                            Text("Star the repository to help others find it, or sponsor ongoing work.", fontSize = 12.sp, color = UDroidTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { /* Star action */ }) {
                            Text("Star on GitHub", fontWeight = FontWeight.Bold, color = UDroidGreen, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(onClick = { /* Sponsor action */ }) {
                            Text("Sponsor", fontWeight = FontWeight.Bold, color = UDroidGreen, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Request Feature / Report Issue Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clickable { /* Issue tracker */ },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
                border = BorderStroke(1.dp, UDroidCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEFF5F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = UDroidTextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Request a feature or report an issue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
                            Text("Open a new issue on GitHub", fontSize = 12.sp, color = UDroidTextSecondary)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = UDroidTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Section: APP UPDATES
        item {
            Text(
                text = "APP UPDATES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = UDroidTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, top = 14.dp, bottom = 6.dp)
            )
        }

        // App Updates Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
                border = BorderStroke(1.dp, UDroidCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEFF5F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = UDroidGreen, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Version 0.1.1", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UDroidTextPrimary)
                            Text("Version 0.1.1 is current", fontSize = 12.sp, color = UDroidTextSecondary)
                        }
                    }

                    TextButton(onClick = { checkUpdateState = "Up to date" }) {
                        Text(
                            text = checkUpdateState,
                            fontWeight = FontWeight.Bold,
                            color = UDroidGreen,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section: SUPERVISOR JOURNAL
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SUPERVISOR JOURNAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UDroidTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Latest ${supervisorLogs.size} events",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = UDroidTextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(
                        onClick = {
                            val report = supervisorLogs.joinToString("\n") { "[${it.timestamp}] ${it.eventTag}: ${it.message}" }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("OS Dockbox Supervisor Report", report))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = UDroidGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy report", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UDroidGreen)
                    }

                    IconButton(
                        onClick = { viewModel.clearSupervisorJournal() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset journal", tint = UDroidTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Supervisor Journal Event Items
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = UDroidCardSurface),
                border = BorderStroke(1.dp, UDroidCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    supervisorLogs.forEach { log ->
                        JournalLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun JournalLogItem(log: SupervisorLogEntity) {
    val dotColor = when (log.level) {
        "SUCCESS" -> Color(0xFF16A34A)
        "ERROR" -> Color(0xFFEF4444)
        "WARN" -> Color(0xFFF59E0B)
        else -> Color(0xFF0284C7)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Column {
                Text(
                    text = log.eventTag,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = UDroidTextPrimary
                )
                Text(
                    text = log.message,
                    fontSize = 12.sp,
                    color = UDroidTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Text(
            text = log.timestamp,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = UDroidTextMuted
        )
    }
}
