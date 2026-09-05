package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.AdminUserViewModel
import com.example.ui.SortOption
import com.example.ui.StatusFilter
import com.example.ui.components.DashboardStatsBar
import com.example.ui.components.FirebasePermissionErrorCard
import com.example.ui.components.UserDetailSheet
import com.example.ui.components.UserListItemCard
import com.example.ui.components.UserTableView
import com.example.ui.dialogs.DeleteUserConfirmDialog
import com.example.ui.dialogs.StatusConfirmDialog
import com.example.ui.dialogs.UpdateCredentialsDialog
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusSuspendedRed
import com.example.ui.theme.VisionEyeBlue
import com.example.util.ExcelExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit = {},
    viewModel: AdminUserViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // Apple Glass Top Bar with vISIONeYe Branding
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // vISIONeYe Iris Vector Logo
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF0F1B38),
                                            Color(0xFF070B14)
                                        )
                                    )
                                )
                                .border(1.dp, VisionEyeBlue.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_visioneye_logo),
                                contentDescription = "vISIONeYe Logo",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "vISIONeYe",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.2).sp
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VisionEyeBlue.copy(alpha = 0.15f))
                                        .border(0.5.dp, VisionEyeBlue.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Admin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = VisionEyeBlue
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "Admin Portal",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 8.dp, end = 12.dp)
                    ) {
                        // 1. Export to Excel Button
                        FilledTonalIconButton(
                            onClick = {
                                val usersToExport = if (state.filteredUsers.isNotEmpty()) {
                                    state.filteredUsers
                                } else {
                                    state.allUsers
                                }
                                if (usersToExport.isEmpty()) {
                                    Toast.makeText(context, "No user data available to export", Toast.LENGTH_SHORT).show()
                                } else {
                                    ExcelExporter.exportUsersToExcel(
                                        context = context,
                                        users = usersToExport,
                                        fileNamePrefix = "vISIONeYe_Users"
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_export_excel"),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0x18107C41),
                                contentColor = Color(0xFF107C41)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_excel_xls),
                                contentDescription = "Export to Excel",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF107C41)
                            )
                        }

                        // 2. Toggle Table / Card view
                        FilledTonalIconButton(
                            onClick = { viewModel.toggleTableView() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_toggle_view"),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = VisionEyeBlue.copy(alpha = 0.12f),
                                contentColor = VisionEyeBlue
                            )
                        ) {
                            Icon(
                                imageVector = if (state.isTableView) Icons.Default.ViewAgenda else Icons.Default.TableChart,
                                contentDescription = "Toggle View Mode",
                                modifier = Modifier.size(18.dp),
                                tint = VisionEyeBlue
                            )
                        }

                        // 3. Refresh Button
                        FilledTonalIconButton(
                            onClick = {
                                Toast.makeText(context, "Refreshing data...", Toast.LENGTH_SHORT).show()
                                viewModel.refreshUsers()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_refresh_users"),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0x14FFFFFF),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Database",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // 4. Logout Button
                        FilledTonalIconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_logout"),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = StatusSuspendedRed.copy(alpha = 0.14f),
                                contentColor = StatusSuspendedRed
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(18.dp),
                                tint = StatusSuspendedRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xF20B1322)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ==================== APPLE TRANSLUCENT SEARCH & SORT CAPSULE ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            "Search",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = VisionEyeBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearSearch() },
                                modifier = Modifier.testTag("btn_clear_search")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = GlassSurfaceElevated,
                        unfocusedContainerColor = GlassSurfaceElevated,
                        focusedBorderColor = VisionEyeBlue,
                        unfocusedBorderColor = GlassBorderStroke.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_search_users")
                )

                // Apple-style Glass Sort Pill Button
                Box {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(GlassSurfaceElevated)
                            .border(
                                1.dp,
                                Brush.linearGradient(listOf(GlassBorderLight, GlassBorderStroke.copy(alpha = 0.3f))),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { isSortMenuExpanded = true }
                            .testTag("btn_sort_menu"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Options",
                            tint = VisionEyeBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false },
                        containerColor = GlassSurfaceElevated
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        fontWeight = if (state.selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                        color = if (state.selectedSort == option) VisionEyeBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    viewModel.onSortOptionChanged(option)
                                    isSortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ==================== STATS OVERVIEW BAR (ACTS AS FILTER SELECTOR) ====================
            DashboardStatsBar(
                stats = state.stats,
                selectedStatusFilter = state.selectedStatusFilter,
                onStatusFilterSelected = { viewModel.onStatusFilterChanged(it) }
            )

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${state.filteredUsers.size} of ${state.allUsers.size} registered customers",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (state.searchQuery.isNotBlank() || state.selectedStatusFilter != StatusFilter.ALL) {
                    Text(
                        text = "Reset Filter",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = VisionEyeBlue
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.clearSearch()
                                viewModel.onStatusFilterChanged(StatusFilter.ALL)
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("btn_reset_filters")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ==================== MAIN CONTENT: LOADING, ERROR, EMPTY, OR LIST ====================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    // 1. Loading State
                    state.isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = VisionEyeBlue,
                                modifier = Modifier.testTag("loading_indicator")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Synchronizing with Firebase Database…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2. Error State with "Try Again" & Permission Diagnosis
                    state.errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            FirebasePermissionErrorCard(
                                errorMessage = state.errorMessage ?: "Database sync issue occurred.",
                                activeCollection = state.activeCollection,
                                onTryAgain = {
                                    Toast.makeText(context, "Refresh data", Toast.LENGTH_SHORT).show()
                                    viewModel.refreshUsers()
                                },
                                onSignInAnonymously = { viewModel.signInAnonymouslyAndRetry() }
                            )
                        }
                    }

                    // 3. Empty Database
                    state.allUsers.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No actual customers found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isFirebaseOnline) {
                                    "Connected to your live Firebase database. Collection '${state.activeCollection}' currently contains 0 records."
                                } else {
                                    "Connecting to Firebase database..."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Refresh data", Toast.LENGTH_SHORT).show()
                                        viewModel.refreshUsers()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VisionEyeBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Refresh Database")
                                }
                            }
                        }
                    }

                    // 4. Search Filter returned 0 results
                    state.filteredUsers.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No customers match your search",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Check the spelling or tap below to reset active filters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearSearch()
                                    viewModel.onStatusFilterChanged(StatusFilter.ALL)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_reset_search_filter")
                            ) {
                                Text("Reset Filters")
                            }
                        }
                    }

                    // 5. Table View
                    state.isTableView -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                        ) {
                            item {
                                UserTableView(
                                    users = state.filteredUsers,
                                    onUserClick = { user -> viewModel.selectUserForDetails(user) },
                                    onEditClick = { user -> viewModel.openUpdateDialog(user) },
                                    onDeleteClick = { user -> viewModel.openDeleteDialog(user) }
                                )
                            }
                        }
                    }

                    // 6. Card List View
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 6.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = state.filteredUsers,
                                key = { it.userId }
                            ) { user ->
                                UserListItemCard(
                                    user = user,
                                    onUserClick = { viewModel.selectUserForDetails(user) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==================== MODALS & DIALOGS ====================

    // 1. User Detail Sheet (With Account Activation Switch, Update Credentials, and Delete Buttons)
    state.selectedUserForDetails?.let { user ->
        UserDetailSheet(
            user = user,
            onDismiss = { viewModel.closeUserDetailSheet() },
            onRequestStatusChange = { targetStatus ->
                viewModel.requestStatusChange(user, targetStatus)
            },
            onActivateClick = {
                viewModel.activateUser(user)
            },
            onBlockClick = {
                viewModel.blockUser(user)
            },
            onUpdateCredentialsClick = {
                viewModel.openUpdateDialog(user)
            },
            onDeleteClick = {
                viewModel.openDeleteDialog(user)
            }
        )
    }

    // 2. Status Confirmation Dialog
    if (state.isStatusConfirmDialogOpen && state.statusChangeTarget != null) {
        val target = state.statusChangeTarget!!
        StatusConfirmDialog(
            user = target.first,
            targetStatus = target.second,
            onConfirm = { viewModel.confirmStatusChange() },
            onDismiss = { viewModel.cancelStatusChange() }
        )
    }

    // 3. Update Credentials Dialog
    if (state.isUpdateDialogOpen && state.userToUpdate != null) {
        UpdateCredentialsDialog(
            user = state.userToUpdate!!,
            onSave = { updatedUser -> viewModel.saveUpdatedUser(updatedUser) },
            onDismiss = { viewModel.closeUpdateDialog() }
        )
    }

    // 4. Delete User Account Confirmation Dialog
    if (state.isDeleteConfirmDialogOpen && state.userToDelete != null) {
        DeleteUserConfirmDialog(
            user = state.userToDelete!!,
            onConfirm = { viewModel.confirmDeleteUser() },
            onDismiss = { viewModel.closeDeleteDialog() }
        )
    }
}
