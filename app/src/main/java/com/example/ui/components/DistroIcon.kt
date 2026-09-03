package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DistroIcon(
    flavor: String,
    modifier: Modifier = Modifier
) {
    val (bg, iconColor) = when (flavor.lowercase()) {
        "debian" -> Color(0xFFFDE8E8) to Color(0xFFD70A53)
        "ubuntu" -> Color(0xFFFFF0EB) to Color(0xFFE95420)
        "alpine" -> Color(0xFFE0F2FE) to Color(0xFF0D597F)
        "arch" -> Color(0xFFE0F2FE) to Color(0xFF1793D1)
        "fedora" -> Color(0xFFE0F2FE) to Color(0xFF3C6EB4)
        "kali" -> Color(0xFFEFF6FF) to Color(0xFF268BD2)
        "opensuse" -> Color(0xFFECFDF5) to Color(0xFF73BA25)
        "void" -> Color(0xFFDCFCE7) to Color(0xFF2A5934)
        "python" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "docker" -> Color(0xFFE0F2FE) to Color(0xFF2496ED)
        "windows", "win", "wine" -> Color(0xFFE0F2FE) to Color(0xFF0078D7)
        "converted" -> Color(0xFFF3E8FF) to Color(0xFF7E22CE)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        when (flavor.lowercase()) {
            "debian" -> {
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawCircle(
                        color = iconColor,
                        radius = size.minDimension / 2.5f,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = iconColor,
                        radius = size.minDimension / 5f,
                        center = Offset(size.width * 0.55f, size.height * 0.45f)
                    )
                }
            }
            "ubuntu" -> {
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawCircle(
                        color = iconColor,
                        radius = size.minDimension / 2.3f,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                    drawCircle(color = iconColor, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.5f))
                    drawCircle(color = iconColor, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.32f, size.height * 0.2f))
                    drawCircle(color = iconColor, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.32f, size.height * 0.8f))
                }
            }
            "alpine" -> {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0.15f)
                        lineTo(size.width * 0.88f, size.height * 0.85f)
                        lineTo(size.width * 0.12f, size.height * 0.85f)
                        close()
                    }
                    drawPath(path, color = iconColor)
                }
            }
            "arch" -> {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0.12f)
                        lineTo(size.width * 0.9f, size.height * 0.88f)
                        lineTo(size.width * 0.5f, size.height * 0.65f)
                        lineTo(size.width * 0.1f, size.height * 0.88f)
                        close()
                    }
                    drawPath(path, color = iconColor)
                }
            }
            "void" -> {
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawCircle(
                        color = iconColor,
                        radius = size.minDimension / 2.4f,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawCircle(
                        color = iconColor,
                        radius = size.minDimension / 6f
                    )
                }
            }
            "docker" -> {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = "Docker",
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            "windows", "win", "wine" -> {
                Icon(
                    imageVector = Icons.Default.DesktopWindows,
                    contentDescription = "Windows Container",
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Distro",
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
