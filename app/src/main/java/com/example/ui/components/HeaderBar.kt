package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun HeaderBar(
    title: String = "OS Dockbox",
    statusText: String = "SESSION LIVE",
    version: String = "v0.1.1",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_osdockbox_logo),
                contentDescription = "OS Dockbox Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = UDroidTextPrimary,
                letterSpacing = (-0.5).sp
            )
        }

        // Right: Status Pill & Version
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live status pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE3F7EB))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16A34A))
                )
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D),
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = version,
                fontSize = 12.sp,
                color = UDroidTextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    type: BadgeType = BadgeType.ACTIVE,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (type) {
        BadgeType.ACTIVE -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Color(0xFF16A34A))
        BadgeType.RECOMMENDED -> Triple(Color(0xFFFFEDD5), Color(0xFFC2410C), Color(0xFFEA580C))
        BadgeType.NEON -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Color(0xFF0284C7))
        BadgeType.MUTED -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFF64748B))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

enum class BadgeType {
    ACTIVE,
    RECOMMENDED,
    NEON,
    MUTED
}
