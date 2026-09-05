package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.User
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.StatusSuspendedRed
import com.example.ui.theme.VisionEyeBlue
import com.example.util.ExcelExporter

@Composable
fun UserTableView(
    users: List<User>,
    onUserClick: (User) -> Unit,
    onEditClick: (User) -> Unit,
    onDeleteClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("users_table_container"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                GlassBorderStroke.copy(alpha = 0.15f)
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
        ) {
            // Table Header Row: | User | Email | Mobile | Company | Country | Status | Action |
            Row(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                VisionEyeBlue.copy(alpha = 0.16f),
                                Color(0xFF5E5CE6).copy(alpha = 0.09f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell(text = "Customer", width = 180.dp)
                TableHeaderCell(text = "Email Address", width = 210.dp)
                TableHeaderCell(text = "Mobile Number", width = 160.dp)
                TableHeaderCell(text = "Company", width = 170.dp)
                TableHeaderCell(text = "Country", width = 130.dp)
                TableHeaderCell(text = "Registration Date", width = 170.dp)
                TableHeaderCell(text = "Status", width = 110.dp)
                TableHeaderCell(text = "Actions", width = 145.dp)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            // Table Rows
            users.forEachIndexed { index, user ->
                val rowBg = if (index % 2 == 0) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                }

                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .clickable { onUserClick(user) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("table_row_${user.userId}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. User (Avatar + Name)
                    Row(
                        modifier = Modifier.width(180.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(VisionEyeBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.initials,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = user.customerName.ifBlank { "Not Available" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 2. Email
                    Text(
                        text = user.email.ifBlank { "Not Available" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(210.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 3. Mobile
                    Text(
                        text = user.fullMobileNumber,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(160.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 4. Company
                    Text(
                        text = user.company.ifBlank { "—" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(170.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 5. Country
                    Text(
                        text = user.country.ifBlank { "—" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(130.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 6. Registration Date
                    val dateFormatted = remember(user.createdAt) {
                        if (user.createdAt > 0) {
                            java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault())
                                .format(java.util.Date(user.createdAt))
                        } else "—"
                    }
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(170.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 7. Status
                    Box(modifier = Modifier.width(110.dp)) {
                        UserStatusBadge(user = user)
                    }

                    // 7. Actions (Export, Edit, Delete)
                    Row(
                        modifier = Modifier.width(145.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Export user Excel file with filename vISIONeYe_Users_<timestamp>.csv
                        IconButton(
                            onClick = {
                                ExcelExporter.exportUsersToExcel(
                                    context = context,
                                    users = listOf(user),
                                    fileNamePrefix = "vISIONeYe_Users"
                                )
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_table_export_${user.userId}")
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_excel_xls),
                                contentDescription = "Export User",
                                tint = Color(0xFF107C41), // Excel emerald green
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onEditClick(user) },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_table_edit_${user.userId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit User",
                                tint = VisionEyeBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (user.userStatus != com.example.model.UserStatus.ACTIVE) {
                            IconButton(
                                onClick = { onDeleteClick(user) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_table_delete_${user.userId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete User",
                                    tint = StatusSuspendedRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.width(width)
    )
}
