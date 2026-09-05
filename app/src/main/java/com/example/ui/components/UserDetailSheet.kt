package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.User
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
import com.example.ui.theme.VisionEyeBlue
import com.example.util.ExcelExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailSheet(
    user: User,
    onDismiss: () -> Unit,
    onRequestStatusChange: (UserStatus) -> Unit,
    onActivateClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
    onUpdateCredentialsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Business rule: Delete button is ONLY shown when status == PENDING or BLOCKED
    // When status == ACTIVE, delete button is hidden.
    val canDeleteUser = user.userStatus != UserStatus.ACTIVE

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xF20B1322),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 36.dp)
        ) {
            // ==================== TOP BAR: Title + Action Icons ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Customer Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Customer Account Information",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Top Bar Action Buttons: Delete (if pending or blocked), Edit, Close with proper spacing
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Delete button: only show when status == PENDING or BLOCKED (never when ACTIVE)
                    if (canDeleteUser) {
                        OutlinedButton(
                            onClick = onDeleteClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = StatusSuspendedRed
                            ),
                            border = BorderStroke(1.dp, StatusSuspendedRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("btn_topbar_delete_customer")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(16.dp),
                                tint = StatusSuspendedRed
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusSuspendedRed
                            )
                        }
                    }

                    // Export button: Exports Excel file with filename vISIONeYe_Users_<timestamp>.csv
                    Button(
                        onClick = {
                            ExcelExporter.exportUsersToExcel(
                                context = context,
                                users = listOf(user),
                                fileNamePrefix = "vISIONeYe_Users"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF107C41), // Microsoft Excel Emerald Green
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_topbar_export_customer")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_excel_xls),
                            contentDescription = "Export Excel",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Edit / Update button: always available in top bar
                    Button(
                        onClick = onUpdateCredentialsClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VisionEyeBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_topbar_update_customer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Close icon button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("btn_close_details")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==================== HERO PROFILE CARD ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xC8101A2D))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color(0x203B82F6)
                            )
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        VisionEyeBlue,
                                        Color(0xFF5856D6)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.initials,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = user.customerName.ifBlank { user.displayName },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = user.email.ifBlank { "No email" },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Status Pill
                    UserStatusBadge(user = user)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==================== STRICT CUSTOMER DETAILS (ONLY 8 REQUESTED FIELDS) ====================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("card_customer_details_strict"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xC8101A2D)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.12f)),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Customer Name
                    CustomerDetailRow(
                        icon = Icons.Default.Person,
                        label = "Customer Name",
                        value = user.customerName.ifBlank { user.displayName },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(user.customerName.ifBlank { user.displayName }))
                            Toast.makeText(context, "Customer Name copied", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 2. Email Address
                    CustomerDetailRow(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = user.email.ifBlank { "Not Available" },
                        onCopy = if (user.email.isNotBlank()) {
                            {
                                clipboardManager.setText(AnnotatedString(user.email))
                                Toast.makeText(context, "Email copied", Toast.LENGTH_SHORT).show()
                            }
                        } else null
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 3. Mobile Number (strictly as it is in Firebase)
                    val mobileDisplay = if (user.fullMobileNumber.isNotBlank() && user.fullMobileNumber != "Not Available") {
                        user.fullMobileNumber
                    } else {
                        "Not Available"
                    }
                    CustomerDetailRow(
                        icon = Icons.Default.Phone,
                        label = "Mobile Number",
                        value = mobileDisplay,
                        onCopy = if (user.fullMobileNumber.isNotBlank() && user.fullMobileNumber != "Not Available") {
                            {
                                clipboardManager.setText(AnnotatedString(user.fullMobileNumber))
                                Toast.makeText(context, "Mobile number copied", Toast.LENGTH_SHORT).show()
                            }
                        } else null
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 4. Company
                    val companyValue = when {
                        user.company.isNotBlank() -> user.company
                        user.businessName.isNotBlank() -> user.businessName
                        else -> "Not Available"
                    }
                    CustomerDetailRow(
                        icon = Icons.Default.Business,
                        label = "Company",
                        value = companyValue,
                        onCopy = if (companyValue != "Not Available") {
                            {
                                clipboardManager.setText(AnnotatedString(companyValue))
                                Toast.makeText(context, "Company copied", Toast.LENGTH_SHORT).show()
                            }
                        } else null
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 5. Country
                    CustomerDetailRow(
                        icon = Icons.Default.Public,
                        label = "Country",
                        value = user.country.ifBlank { "Not Available" },
                        onCopy = if (user.country.isNotBlank()) {
                            {
                                clipboardManager.setText(AnnotatedString(user.country))
                                Toast.makeText(context, "Country copied", Toast.LENGTH_SHORT).show()
                            }
                        } else null
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 6. City
                    CustomerDetailRow(
                        icon = Icons.Default.LocationCity,
                        label = "City",
                        value = user.city.ifBlank { "Not Available" },
                        onCopy = if (user.city.isNotBlank()) {
                            {
                                clipboardManager.setText(AnnotatedString(user.city))
                                Toast.makeText(context, "City copied", Toast.LENGTH_SHORT).show()
                            }
                        } else null
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 7. Account Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (user.userStatus) {
                                            UserStatus.ACTIVE -> StatusActiveGreen
                                            UserStatus.PENDING -> StatusPendingAmber
                                            UserStatus.BLOCKED -> StatusSuspendedRed
                                        }.copy(alpha = 0.14f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = when (user.userStatus) {
                                        UserStatus.ACTIVE -> StatusActiveGreen
                                        UserStatus.PENDING -> StatusPendingAmber
                                        UserStatus.BLOCKED -> StatusSuspendedRed
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Account Status",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = user.userStatus.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = when (user.userStatus) {
                                        UserStatus.ACTIVE -> StatusActiveGreen
                                        UserStatus.PENDING -> StatusPendingAmber
                                        UserStatus.BLOCKED -> StatusSuspendedRed
                                    }
                                )
                            }
                        }

                        // Activation switch for quick toggle
                        Switch(
                            checked = user.userStatus == UserStatus.ACTIVE,
                            onCheckedChange = { isChecked ->
                                if (isChecked) onActivateClick() else onBlockClick()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusActiveGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = StatusSuspendedRed.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("switch_account_activation")
                        )
                    }

                    // Direct Status Chips (Active, Pending, Blocked)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserStatus.entries.forEach { statusOption ->
                            val isCurrent = user.userStatus == statusOption
                            FilterChip(
                                selected = isCurrent,
                                onClick = {
                                    if (statusOption == UserStatus.ACTIVE) {
                                        onActivateClick()
                                    } else if (statusOption == UserStatus.BLOCKED) {
                                        onBlockClick()
                                    } else {
                                        onRequestStatusChange(statusOption)
                                    }
                                },
                                label = {
                                    Text(
                                        text = statusOption.label,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_set_status_${statusOption.value}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (statusOption) {
                                        UserStatus.ACTIVE -> StatusActiveGreen
                                        UserStatus.PENDING -> StatusPendingAmber
                                        UserStatus.BLOCKED -> StatusSuspendedRed
                                    },
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 8. Created At Date
                    CustomerDetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Created At",
                        value = if (user.createdAt > 0) formatDateTime(user.createdAt) else "Not Available"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 9. Updated At Date
                    CustomerDetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Updated At",
                        value = if (user.updatedAt > 0) formatDateTime(user.updatedAt) else "Not Available"
                    )

                    val loginAt = user.lastLoginAt
                    if (loginAt != null && loginAt > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        CustomerDetailRow(
                            icon = Icons.Default.Schedule,
                            label = "Last Login At",
                            value = formatDateTime(loginAt)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // If user is pending, show 1-click Activate button at bottom
            if (user.isPending) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Button(
                        onClick = onActivateClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_activate_user"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusActiveGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Approve & Activate Account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VisionEyeBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VisionEyeBlue,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (onCopy != null) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    if (timestamp <= 0) return "Not Available"
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
