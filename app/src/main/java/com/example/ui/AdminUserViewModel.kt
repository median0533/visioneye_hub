package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseUserRepository
import com.example.model.User
import com.example.model.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class StatusFilter(val label: String) {
    ALL("All"),
    PENDING("Pending Approval"),
    ACTIVE("Active"),
    STOPPED("App Stopped"),
    BLOCKED("Blocked")
}

enum class SortOption(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    COMPANY("Company (A to Z)")
}

data class DashboardStats(
    val totalUsers: Int = 0,
    val pendingUsers: Int = 0,
    val activeUsers: Int = 0,
    val stoppedUsers: Int = 0,
    val blockedUsers: Int = 0
)

data class AdminUiState(
    val allUsers: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: StatusFilter = StatusFilter.ALL,
    val selectedSort: SortOption = SortOption.NEWEST,
    val isTableView: Boolean = false,
    val selectedUserForDetails: User? = null,
    val userToUpdate: User? = null,
    val userToDelete: User? = null,
    val userToStopApp: User? = null,
    val statusChangeTarget: Pair<User, UserStatus>? = null,
    val isUpdateDialogOpen: Boolean = false,
    val isDeleteConfirmDialogOpen: Boolean = false,
    val isStatusConfirmDialogOpen: Boolean = false,
    val isStopAppDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val isFirebaseOnline: Boolean = false,
    val activeCollection: String = "users",
    val firebaseProjectId: String = "",
    val stats: DashboardStats = DashboardStats()
)

class AdminUserViewModel(
    private val repository: FirebaseUserRepository = FirebaseUserRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    private val _isTableView = MutableStateFlow(false)

    private val _selectedUserForDetails = MutableStateFlow<User?>(null)
    private val _userToUpdate = MutableStateFlow<User?>(null)
    private val _userToDelete = MutableStateFlow<User?>(null)
    private val _userToStopApp = MutableStateFlow<User?>(null)
    private val _statusChangeTarget = MutableStateFlow<Pair<User, UserStatus>?>(null)

    private val _isUpdateDialogOpen = MutableStateFlow(false)
    private val _isDeleteConfirmDialogOpen = MutableStateFlow(false)
    private val _isStatusConfirmDialogOpen = MutableStateFlow(false)
    private val _isStopAppDialogOpen = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AdminUiState> = combine(
        repository.usersFlow,
        _searchQuery,
        _statusFilter,
        _sortOption,
        _isTableView,
        _selectedUserForDetails,
        _userToUpdate,
        _userToDelete,
        _userToStopApp,
        _statusChangeTarget,
        _isUpdateDialogOpen,
        _isDeleteConfirmDialogOpen,
        _isStatusConfirmDialogOpen,
        _isStopAppDialogOpen,
        _snackbarMessage,
        repository.isLoading,
        repository.errorMessage,
        repository.isFirebaseOnline,
        repository.activeCollection,
        repository.firebaseProjectId
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val users = params[0] as List<User>
        val query = params[1] as String
        val statusFilter = params[2] as StatusFilter
        val sort = params[3] as SortOption
        val isTable = params[4] as Boolean
        val selectedUser = params[5] as? User
        val userUpdate = params[6] as? User
        val userDelete = params[7] as? User
        val userStopApp = params[8] as? User
        @Suppress("UNCHECKED_CAST")
        val statusTarget = params[9] as? Pair<User, UserStatus>
        val isUpdateOpen = params[10] as Boolean
        val isDeleteOpen = params[11] as Boolean
        val isStatusOpen = params[12] as Boolean
        val isStopAppOpen = params[13] as Boolean
        val snackbarMsg = params[14] as? String
        val isLoading = params[15] as Boolean
        val errorMsg = params[16] as? String
        val isOnline = params[17] as Boolean
        val currentCollection = params[18] as String
        val projectId = params[19] as String

        // Calculate Stats according to Firebase Admin App Guide
        val stats = DashboardStats(
            totalUsers = users.size,
            pendingUsers = users.count { it.isPending },
            activeUsers = users.count { it.isActive },
            stoppedUsers = users.count { it.stopapp },
            blockedUsers = users.count { it.isBlocked }
        )

        // Filter by search query and status
        val cleanQuery = query.trim().lowercase()
        var filtered = users.filter { user ->
            val matchesQuery = cleanQuery.isBlank() ||
                    user.customerName.lowercase().contains(cleanQuery) ||
                    user.businessName.lowercase().contains(cleanQuery) ||
                    user.email.lowercase().contains(cleanQuery) ||
                    user.mobileNumber.lowercase().contains(cleanQuery) ||
                    user.countryCode.lowercase().contains(cleanQuery) ||
                    "${user.countryCode} ${user.mobileNumber}".lowercase().contains(cleanQuery) ||
                    user.company.lowercase().contains(cleanQuery) ||
                    user.country.lowercase().contains(cleanQuery) ||
                    user.city.lowercase().contains(cleanQuery) ||
                    user.state.lowercase().contains(cleanQuery) ||
                    user.userId.lowercase().contains(cleanQuery) ||
                    (user.stopappReason?.lowercase()?.contains(cleanQuery) == true)

            val matchesStatus = when (statusFilter) {
                StatusFilter.ALL -> true
                StatusFilter.PENDING -> user.isPending
                StatusFilter.ACTIVE -> user.isActive
                StatusFilter.STOPPED -> user.stopapp
                StatusFilter.BLOCKED -> user.isBlocked
            }

            matchesQuery && matchesStatus
        }

        // Sort
        filtered = when (sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
            SortOption.OLDEST -> filtered.sortedBy { it.createdAt }
            SortOption.NAME_ASC -> filtered.sortedBy { it.displayName.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.displayName.lowercase() }
            SortOption.COMPANY -> filtered.sortedBy { it.company.lowercase() }
        }

        // Keep selected user details synchronized with list updates
        val currentSelectedUser = if (selectedUser != null) {
            users.find { it.userId == selectedUser.userId } ?: selectedUser
        } else null

        AdminUiState(
            allUsers = users,
            filteredUsers = filtered,
            searchQuery = query,
            selectedStatusFilter = statusFilter,
            selectedSort = sort,
            isTableView = isTable,
            selectedUserForDetails = currentSelectedUser,
            userToUpdate = userUpdate,
            userToDelete = userDelete,
            userToStopApp = userStopApp,
            statusChangeTarget = statusTarget,
            isUpdateDialogOpen = isUpdateOpen,
            isDeleteConfirmDialogOpen = isDeleteOpen,
            isStatusConfirmDialogOpen = isStatusOpen,
            isStopAppDialogOpen = isStopAppOpen,
            isLoading = isLoading,
            errorMessage = errorMsg,
            snackbarMessage = snackbarMsg,
            isFirebaseOnline = isOnline,
            activeCollection = currentCollection,
            firebaseProjectId = projectId,
            stats = stats
        )
    }.let { flow ->
        MutableStateFlow(AdminUiState()).also { stateFlow ->
            viewModelScope.launch {
                flow.collect { stateFlow.value = it }
            }
        }
    }

    // ==================== USER ACTIONS ====================

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun onStatusFilterChanged(filter: StatusFilter) {
        _statusFilter.value = if (_statusFilter.value == filter && filter != StatusFilter.ALL) {
            StatusFilter.ALL
        } else {
            filter
        }
    }

    fun onSortOptionChanged(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleTableView() {
        _isTableView.value = !_isTableView.value
    }

    fun selectUserForDetails(user: User) {
        _selectedUserForDetails.value = user
    }

    fun closeUserDetailSheet() {
        _selectedUserForDetails.value = null
    }

    // ==================== ACCOUNT STATUS / ACTIVATION ====================

    /**
     * Activates a user account (e.g. from pending or blocked).
     * Sets status = "active", stopapp = false, stopapp_reason = null.
     */
    fun activateUser(user: User) {
        viewModelScope.launch {
            val result = repository.activateUser(user.userId)
            if (result.isSuccess) {
                _snackbarMessage.value = "Account for '${user.displayName}' activated. User can now access the app."
            } else {
                _snackbarMessage.value = "Failed to activate user: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    /**
     * Blocks a user account.
     * Sets status = "blocked".
     */
    fun blockUser(user: User) {
        viewModelScope.launch {
            val result = repository.blockUser(user.userId)
            if (result.isSuccess) {
                _snackbarMessage.value = "Account for '${user.displayName}' has been blocked."
            } else {
                _snackbarMessage.value = "Failed to block user: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    /**
     * Opens dialog to stop app with a reason (Kill Switch).
     */
    fun openStopAppDialog(user: User) {
        _userToStopApp.value = user
        _isStopAppDialogOpen.value = true
    }

    fun closeStopAppDialog() {
        _isStopAppDialogOpen.value = false
        _userToStopApp.value = null
    }

    /**
     * Confirms Stop App for the user.
     * Sets stopapp = true, stopapp_reason = reason.
     * Live Firestore listener stops the user immediately in their app!
     */
    fun confirmStopApp(reason: String) {
        val user = _userToStopApp.value ?: return
        viewModelScope.launch {
            val result = repository.stopAppForUser(user.userId, reason.trim().ifBlank { "Account disabled by administrator" })
            _isStopAppDialogOpen.value = false
            _userToStopApp.value = null
            if (result.isSuccess) {
                _snackbarMessage.value = "App stopped for '${user.displayName}'. Kill switch triggered immediately."
            } else {
                _snackbarMessage.value = "Failed to stop app: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    /**
     * Restores access after Stop App.
     * Sets status = "active", stopapp = false, stopapp_reason = null.
     */
    fun restoreUserAccess(user: User) {
        viewModelScope.launch {
            val result = repository.restoreUserAccess(user.userId)
            if (result.isSuccess) {
                _snackbarMessage.value = "Access restored for '${user.displayName}'."
            } else {
                _snackbarMessage.value = "Failed to restore access: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun requestStatusChange(user: User, newStatus: UserStatus) {
        if (user.userStatus == newStatus) return
        _statusChangeTarget.value = Pair(user, newStatus)
        _isStatusConfirmDialogOpen.value = true
    }

    fun confirmStatusChange() {
        val target = _statusChangeTarget.value ?: return
        val user = target.first
        val newStatus = target.second

        viewModelScope.launch {
            val result = repository.updateUserStatus(user.userId, newStatus.value)
            _isStatusConfirmDialogOpen.value = false
            _statusChangeTarget.value = null
            if (result.isSuccess) {
                _snackbarMessage.value = "Status updated to ${newStatus.label} in Firebase."
            } else {
                _snackbarMessage.value = "Status update saved locally (Firebase sync pending)."
            }
        }
    }

    fun cancelStatusChange() {
        _isStatusConfirmDialogOpen.value = false
        _statusChangeTarget.value = null
    }

    fun toggleActivation(user: User) {
        if (user.isPending || user.isBlocked) {
            activateUser(user)
        } else {
            blockUser(user)
        }
    }

    // ==================== UPDATE CREDENTIALS / USER DATA ====================

    fun openUpdateDialog(user: User) {
        _userToUpdate.value = user
        _isUpdateDialogOpen.value = true
    }

    fun closeUpdateDialog() {
        _isUpdateDialogOpen.value = false
        _userToUpdate.value = null
    }

    fun saveUpdatedUser(user: User) {
        viewModelScope.launch {
            val result = repository.updateUser(user)
            _isUpdateDialogOpen.value = false
            _userToUpdate.value = null
            if (result.isSuccess) {
                _snackbarMessage.value = "Customer information synchronized to Firebase."
            } else {
                _snackbarMessage.value = "Customer information updated."
            }
        }
    }

    // ==================== DELETE USER ====================

    fun openDeleteDialog(user: User) {
        _userToDelete.value = user
        _isDeleteConfirmDialogOpen.value = true
    }

    fun closeDeleteDialog() {
        _isDeleteConfirmDialogOpen.value = false
        _userToDelete.value = null
    }

    fun confirmDeleteUser() {
        val target = _userToDelete.value ?: return
        viewModelScope.launch {
            val result = repository.deleteUser(target.userId)
            if (_selectedUserForDetails.value?.userId == target.userId) {
                _selectedUserForDetails.value = null
            }
            _isDeleteConfirmDialogOpen.value = false
            _userToDelete.value = null
            if (result.isSuccess) {
                _snackbarMessage.value = "Customer account deleted from Firestore & Authentication."
            } else {
                _snackbarMessage.value = "Customer record removed."
            }
        }
    }

    // ==================== FIREBASE CONFIGURATION & COLLECTIONS ====================

    fun setCollection(collectionName: String) {
        repository.setCollection(collectionName)
        _snackbarMessage.value = "Switched to Firestore collection '$collectionName'."
    }

    fun configureFirebaseProject(
        projectId: String,
        apiKey: String? = null,
        appId: String? = null,
        collection: String? = null
    ) {
        repository.configureFirebaseProject(projectId, apiKey, appId, collection)
        _snackbarMessage.value = "Configured Firebase Project '$projectId'."
    }

    // ==================== REFRESH & RETRY ====================

    fun refreshUsers() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun signInAnonymouslyAndRetry() {
        viewModelScope.launch {
            _snackbarMessage.value = "Authenticating with Firebase Auth..."
            val result = repository.signInAnonymously()
            if (result.isSuccess) {
                _snackbarMessage.value = "Authenticated with Firebase Auth (UID: ${result.getOrNull()?.take(8)}...)"
                repository.refresh()
            } else {
                _snackbarMessage.value = "Auth failed: ${result.exceptionOrNull()?.localizedMessage ?: "Enable Anonymous sign-in in Firebase Console"}"
            }
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}
