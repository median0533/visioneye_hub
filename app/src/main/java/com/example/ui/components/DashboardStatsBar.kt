package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DashboardStats
import com.example.ui.StatusFilter
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusSuspendedRed
import com.example.ui.theme.VisionEyeBlue

@Composable
fun DashboardStatsBar(
    stats: DashboardStats,
    selectedStatusFilter: StatusFilter,
    onStatusFilterSelected: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Total Users Card
        GlassStatCard(
            title = "All Customers",
            count = stats.totalUsers,
            icon = Icons.Default.People,
            accentColor = VisionEyeBlue,
            isSelected = selectedStatusFilter == StatusFilter.ALL,
            onClick = { onStatusFilterSelected(StatusFilter.ALL) },
            testTag = "stat_card_total"
        )

        // Pending Approval Users (Amber - Priority Approval Queue)
        GlassStatCard(
            title = "Pending Approval",
            count = stats.pendingUsers,
            icon = Icons.Default.HourglassTop,
            accentColor = StatusPendingAmber,
            isSelected = selectedStatusFilter == StatusFilter.PENDING,
            onClick = { onStatusFilterSelected(StatusFilter.PENDING) },
            testTag = "stat_card_pending"
        )

        // Active Users (Apple Green)
        GlassStatCard(
            title = "Active Accounts",
            count = stats.activeUsers,
            icon = Icons.Default.CheckCircle,
            accentColor = StatusActiveGreen,
            isSelected = selectedStatusFilter == StatusFilter.ACTIVE,
            onClick = { onStatusFilterSelected(StatusFilter.ACTIVE) },
            testTag = "stat_card_active"
        )

        // Blocked Users (Muted Red)
        GlassStatCard(
            title = "Blocked",
            count = stats.blockedUsers,
            icon = Icons.Default.Block,
            accentColor = Color(0xFFE53935),
            isSelected = selectedStatusFilter == StatusFilter.BLOCKED,
            onClick = { onStatusFilterSelected(StatusFilter.BLOCKED) },
            testTag = "stat_card_blocked"
        )
    }
}

@Composable
private fun GlassStatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val containerColor = if (isSelected) {
        accentColor.copy(alpha = 0.18f)
    } else {
        Color(0xC8101A2D)
    }

    val borderBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.9f),
                accentColor.copy(alpha = 0.4f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color(0x253B82F6)
            )
        )
    }

    Card(
        modifier = Modifier
            .width(142.dp)
            .testTag(testTag)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = if (isSelected) 0.22f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Filter",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = count,
                label = "stat_count_anim"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
