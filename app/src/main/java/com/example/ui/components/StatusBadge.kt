package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserStatus
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusActiveGreenBg
import com.example.ui.theme.StatusActiveGreenBorder
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingAmberBg
import com.example.ui.theme.StatusPendingAmberBorder
import com.example.ui.theme.StatusSuspendedRed
import com.example.ui.theme.StatusSuspendedRedBg
import com.example.ui.theme.StatusSuspendedRedBorder

@Composable
fun StatusBadge(
    status: UserStatus,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    val (bgColor, textColor, borderColor) = when (status) {
        UserStatus.ACTIVE -> Triple(StatusActiveGreenBg, StatusActiveGreen, StatusActiveGreenBorder)
        UserStatus.PENDING -> Triple(StatusPendingAmberBg, StatusPendingAmber, StatusPendingAmberBorder)
        UserStatus.BLOCKED -> Triple(StatusSuspendedRedBg, StatusSuspendedRed, StatusSuspendedRedBorder)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(textColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }

            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.3.sp
                ),
                color = textColor
            )
        }
    }
}

@Composable
fun UserStatusBadge(
    user: com.example.model.User,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    StatusBadge(status = user.userStatus, modifier = modifier, showDot = showDot)
}

