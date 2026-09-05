package com.example.data

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.AdminApplication
import com.example.model.FirebaseConfigInfo
import com.example.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Backend-connected Repository managing real User customer data in Firebase Firestore.
 * Strictly fetches actual data from Firestore with zero dummy/sample data.
 */
class FirebaseUserRepository(
    private val context: Context? = null
) {
    private val tag = "FirebaseUserRepo"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _usersFlow = MutableStateFlow<List<User>>(emptyList())
    val usersFlow: StateFlow<List<User>> = _usersFlow.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isFirebaseOnline = MutableStateFlow(false)
    val isFirebaseOnline: StateFlow<Boolean> = _isFirebaseOnline.asStateFlow()

    private val _activeCollection = MutableStateFlow("users")
    val activeCollection: StateFlow<String> = _activeCollection.asStateFlow()

    private val _firebaseProjectId = MutableStateFlow<String>("visioneye-a04dd")
    val firebaseProjectId: StateFlow<String> = _firebaseProjectId.asStateFlow()

    private var snapshotListener: ListenerRegistration? = null
    private var firestore: FirebaseFirestore? = null
    private var isInitialLoadComplete = false

    private val appContext: Context?
        get() = context 
            ?: AdminApplication.instance 
            ?: runCatching { FirebaseApp.getInstance().applicationContext }.getOrNull()

    init {
        loadSavedConfig()
        initializeFirebase()
    }

    private fun loadSavedConfig() {
        appContext?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences("firebase_hub_prefs", Context.MODE_PRIVATE)
                val savedCollection = prefs.getString("collection_name", "users") ?: "users"
                _activeCollection.value = savedCollection

                val savedProjectId = prefs.getString("project_id", "") ?: ""
                if (savedProjectId.isNotBlank()) {
                    _firebaseProjectId.value = savedProjectId
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to read saved preferences: ${e.message}")
            }
        }
    }

    fun initializeFirebase() {
        _isLoading.value = true
        _errorMessage.value = null

        val db = getOrInitFirestore()
        if (db != null) {
            listenToCollection(db, _activeCollection.value)
        } else {
            _isLoading.value = false
            _isFirebaseOnline.value = false
            // Note: Keep _usersFlow as emptyList() - NO dummy data
            _usersFlow.value = emptyList()
        }
    }

    private fun getOrInitFirestore(): FirebaseFirestore? {
        try {
            firestore?.let { return it }

            // Check if FirebaseApp is already initialized
            var app: FirebaseApp? = runCatching { FirebaseApp.getInstance() }.getOrNull()

            if (app == null) {
                val ctx = appContext
                if (ctx != null) {
                    val apps = FirebaseApp.getApps(ctx)
                    if (apps.isNotEmpty()) {
                        app = apps.first()
                    } else {
                        // Check if we have a saved project ID from SharedPreferences
                        val prefs = ctx.getSharedPreferences("firebase_hub_prefs", Context.MODE_PRIVATE)
                        val savedProjectId = prefs.getString("project_id", null)
                        val savedApiKey = prefs.getString("api_key", null)
                        val savedAppId = prefs.getString("app_id", null)

                        if (!savedProjectId.isNullOrBlank()) {
                            val options = FirebaseOptions.Builder()
                                .setProjectId(savedProjectId)
                                .setApiKey(savedApiKey?.ifBlank { "AIzaSyA_LHu-abZKQUmS48aeoQXnwpxBT1YFyWw" } ?: "AIzaSyA_LHu-abZKQUmS48aeoQXnwpxBT1YFyWw")
                                .setApplicationId(savedAppId?.ifBlank { "1:189246600899:android:9f13bb9d10f53b398d9476" } ?: "1:189246600899:android:9f13bb9d10f53b398d9476")
                                .build()
                            app = runCatching { FirebaseApp.initializeApp(ctx, options) }.getOrNull()
                        } else {
                            // First try default init from google-services resources
                            app = runCatching { FirebaseApp.initializeApp(ctx) }.getOrNull()

                            // If default init fails, use explicit google-services.json options
                            if (app == null) {
                                val options = FirebaseOptions.Builder()
                                    .setProjectId("visioneye-a04dd")
                                    .setApiKey("AIzaSyA_LHu-abZKQUmS48aeoQXnwpxBT1YFyWw")
                                    .setApplicationId("1:189246600899:android:9f13bb9d10f53b398d9476")
                                    .setStorageBucket("visioneye-a04dd.firebasestorage.app")
                                    .setGcmSenderId("189246600899")
                                    .build()
                                app = runCatching { FirebaseApp.initializeApp(ctx, options) }.getOrNull()
                            }
                        }
                    }
                }
            }

            if (app != null) {
                _firebaseProjectId.value = app.options.projectId ?: "visioneye-a04dd"
                val db = FirebaseFirestore.getInstance(app)
                firestore = db
                return db
            } else {
                // Last fallback: attempt to get default instance if already initialized by Android OS
                val db = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
                if (db != null) {
                    firestore = db
                    return db
                }
                _errorMessage.value = "Firebase connection pending: FirebaseApp not initialized"
                _isFirebaseOnline.value = false
                return null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error initializing Firestore: ${e.message}", e)
            _errorMessage.value = "Firebase connection pending: ${e.localizedMessage ?: e.message}"
            _isFirebaseOnline.value = false
            return null
        }
    }

    private fun listenToCollection(db: FirebaseFirestore, collectionName: String) {
        try {
            snapshotListener?.remove()
            _isLoading.value = true

            snapshotListener = db.collection(collectionName)
                .addSnapshotListener { snapshots, error ->
                    _isLoading.value = false

                    if (error != null) {
                        Log.e(tag, "Firestore snapshot error on '$collectionName': ${error.message}")
                        _isFirebaseOnline.value = false
                        _errorMessage.value = error.localizedMessage ?: "Firestore error: ${error.message}"
                        // Strictly NO dummy data
                        return@addSnapshotListener
                    }

                    _isFirebaseOnline.value = true
                    _errorMessage.value = null

                    if (snapshots != null && !snapshots.isEmpty) {
                        val actualUsers = snapshots.documents.map { doc ->
                            mapDocumentToUser(doc)
                        }.filter { it.deletedAt == null && it.status != "deleted" }
                        _usersFlow.value = actualUsers
                        Log.d(tag, "Successfully loaded ${actualUsers.size} actual users from Firebase collection '$collectionName'")

                        // Trigger notifications IMMEDIATELY for new registered users or pending users
                        context?.let { ctx ->
                            try {
                                val prefs = ctx.getSharedPreferences("firebase_hub_prefs", Context.MODE_PRIVATE)
                                val notifiedUsers = prefs.getStringSet("notified_user_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                                var updatedNotified = false

                                for (user in actualUsers) {
                                    // Check if this user hasn't been notified yet
                                    if (!notifiedUsers.contains(user.userId)) {
                                        val isRecentOrPending = user.isPending || 
                                            (System.currentTimeMillis() - user.createdAt < 48 * 60 * 60 * 1000L) || 
                                            isInitialLoadComplete

                                        if (isRecentOrPending) {
                                            Log.i(tag, "Triggering immediate alert for customer: ${user.userId} (${user.displayName})")
                                            com.example.util.NewUserNotificationHelper.showNewUserNotification(
                                                ctx,
                                                user.userId,
                                                user.displayName,
                                                user.email
                                            )
                                            notifiedUsers.add(user.userId)
                                            updatedNotified = true
                                        }
                                    }
                                }
                                if (updatedNotified) {
                                    prefs.edit().putStringSet("notified_user_ids", notifiedUsers).apply()
                                }
                            } catch (e: Exception) {
                                Log.e(tag, "Immediate notification check error: ${e.message}")
                            }
                        }
                        isInitialLoadComplete = true
                    } else {
                        Log.d(tag, "Firestore collection '$collectionName' returned 0 documents.")
                        _usersFlow.value = emptyList()

                        // If default 'users' is empty, probe 'Users' or 'customers' once to assist user
                        if (collectionName == "users") {
                            scope.launch {
                                checkAlternativeCollections(db)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach snapshot listener: ${e.message}")
            _isFirebaseOnline.value = false
            _errorMessage.value = e.localizedMessage ?: "Connection error"
            _isLoading.value = false
        }
    }

    /**
     * Checks if alternative commonly capitalized collections exist (e.g. 'Users' or 'customers')
     */
    private suspend fun checkAlternativeCollections(db: FirebaseFirestore) {
        val alternatives = listOf("Users", "customers", "Customers")
        for (alt in alternatives) {
            try {
                val snap = db.collection(alt).limit(1).get().await()
                if (!snap.isEmpty && _usersFlow.value.isEmpty()) {
                    Log.i(tag, "Discovered records in alternative collection '$alt'. Auto-connecting to '$alt'.")
                    setCollection(alt)
                    break
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Maps any Firestore DocumentSnapshot into a clean User model.
     * Safely parses varied naming styles (name, fullName, displayName, mobile, contact, phone),
     * and handles Timestamps, Dates, Numbers, and Strings without crashing.
     */
    private fun mapDocumentToUser(doc: DocumentSnapshot): User {
        val data = doc.data ?: emptyMap<String, Any>()
        val id = doc.id

        // 1. Customer Name (lenient fallback chain)
        val name = (
            data["customerName"]
                ?: data["name"]
                ?: data["fullName"]
                ?: data["full_name"]
                ?: data["displayName"]
                ?: data["userName"]
                ?: data["username"]
                ?: run {
                    val first = data["firstName"] ?: data["first_name"] ?: ""
                    val last = data["lastName"] ?: data["last_name"] ?: ""
                    val combined = "$first $last".trim()
                    if (combined.isNotBlank()) combined else null
                }
        )?.toString()?.trim() ?: ""

        // 2. Email Address
        val email = (
            data["email"]
                ?: data["userEmail"]
                ?: data["mail"]
                ?: data["emailAddress"]
        )?.toString()?.trim() ?: ""

        // 3. Mobile Number directly from Firebase as it is without extra logic
        val mobile = (
            data["mobile_number"]
                ?: data["mobileNumber"]
                ?: data["mobile"]
                ?: data["phone"]
                ?: data["phoneNumber"]
                ?: data["phone_number"]
                ?: data["contact"]
                ?: data["contactNumber"]
                ?: data["contact_number"]
                ?: data["tel"]
                ?: data["cell"]
        )?.toString()?.trim() ?: ""

        val countryCode = (
            data["countryCode"]
                ?: data["country_code"]
                ?: data["dialCode"]
                ?: data["dial_code"]
        )?.toString()?.trim() ?: ""

        // 5. Company / Business Name
        val businessName = (
            data["business_name"]
                ?: data["businessName"]
                ?: data["company"]
                ?: data["companyName"]
                ?: data["organization"]
                ?: data["org"]
                ?: data["business"]
        )?.toString()?.trim() ?: ""

        // 6. Address and Location
        val address = (
            data["address"]
                ?: data["street"]
                ?: data["streetAddress"]
                ?: data["addressLine"]
                ?: data["addressLine1"]
        )?.toString()?.trim() ?: ""

        val city = (data["city"] ?: data["town"])?.toString()?.trim() ?: ""
        val state = (data["state"] ?: data["province"] ?: data["region"])?.toString()?.trim() ?: ""
        val country = (data["country"] ?: data["nation"])?.toString()?.trim() ?: ""
        val pincode = (
            data["pincode"]
                ?: data["pinCode"]
                ?: data["zip"]
                ?: data["zipcode"]
                ?: data["postalCode"]
                ?: data["postal_code"]
        )?.toString()?.trim() ?: ""

        // Phone verified
        val phoneVerified = (data["phone_verified"] as? Boolean)
            ?: (data["phoneVerified"] as? Boolean)
            ?: (data["isPhoneVerified"] as? Boolean)
            ?: false

        // Stop app kill switch and reason
        val stopapp = (data["stopapp"] as? Boolean)
            ?: (data["stopApp"] as? Boolean)
            ?: false

        val stopappReason = (data["stopapp_reason"] ?: data["stopappReason"])?.toString()

        // 7. Status Resolution
        val rawStatus = data["status"] ?: data["userStatus"] ?: data["accountStatus"] ?: data["state"]
        val status = when {
            rawStatus != null -> {
                val s = rawStatus.toString().lowercase().trim()
                when {
                    s.contains("active") || s == "true" || s == "verified" || s == "enabled" -> "active"
                    s.contains("block") || s.contains("suspend") || s.contains("banned") || s == "disabled" -> "blocked"
                    s.contains("pend") || s.contains("wait") || s.contains("review") -> "pending"
                    else -> s
                }
            }
            data["isActive"] == true || data["active"] == true || data["enabled"] == true -> "active"
            data["isBlocked"] == true || data["blocked"] == true || data["disabled"] == true -> "blocked"
            data["isPending"] == true || data["pending"] == true -> "pending"
            else -> "active"
        }

        // 8. Timestamps (safe parsing of Firebase Timestamp, Date, Long, String)
        val createdAt = parseTimestamp(
            data["createdAt"] ?: data["created_at"] ?: data["timestamp"] ?: data["dateCreated"] ?: data["registeredAt"]
        )
        val updatedAt = parseTimestamp(
            data["updatedAt"] ?: data["updated_at"] ?: data["lastModified"] ?: data["modifiedAt"]
        )
        val lastLoginAt = parseTimestampNullable(data["last_login_at"] ?: data["lastLoginAt"])
        val deletedAt = parseTimestampNullable(data["deleted_at"] ?: data["deletedAt"])

        // 9. Avatar Color
        val avatarColorHex = (data["avatarColorHex"] ?: data["avatarColor"])?.toString()
            ?: generateAvatarColor(id, name.ifBlank { businessName.ifBlank { email } })

        // Derive user-friendly display name
        val finalName = when {
            businessName.isNotBlank() -> businessName
            name.isNotBlank() -> name
            email.isNotBlank() -> email.substringBefore("@")
            else -> "Customer ${id.take(6)}"
        }

        val finalUid = (data["firebase_uid"] ?: data["uid"] ?: id).toString()

        return User(
            userId = id,
            firebaseUid = finalUid,
            customerName = if (name.isNotBlank()) name else finalName,
            businessName = businessName,
            email = email,
            countryCode = countryCode,
            mobileNumber = mobile,
            company = businessName,
            address = address,
            country = country,
            state = state,
            city = city,
            pincode = pincode,
            phoneVerified = phoneVerified,
            status = status,
            stopapp = stopapp,
            stopappReason = stopappReason,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastLoginAt = lastLoginAt,
            deletedAt = deletedAt,
            avatarColorHex = avatarColorHex
        )
    }

    private fun parseTimestampNullable(value: Any?): Long? {
        if (value == null) return null
        return when (value) {
            is com.google.firebase.Timestamp -> value.toDate().time
            is java.util.Date -> value.time
            is Number -> {
                val num = value.toLong()
                // If timestamp is in seconds (e.g. 10 digits ~ 1.7e9), convert to milliseconds
                if (num in 1_000_000_000L..99_999_999_999L) {
                    num * 1000L
                } else {
                    num
                }
            }
            is String -> {
                val trimmed = value.trim()
                val longVal = trimmed.toLongOrNull()
                if (longVal != null) {
                    if (longVal in 1_000_000_000L..99_999_999_999L) {
                        longVal * 1000L
                    } else {
                        longVal
                    }
                } else {
                    parseDateStringToEpochMilli(trimmed)
                }
            }
            else -> null
        }
    }

    private fun parseTimestamp(value: Any?): Long {
        return parseTimestampNullable(value) ?: System.currentTimeMillis()
    }

    private fun parseDateStringToEpochMilli(value: String): Long? {
        if (value.isBlank()) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return Instant.parse(value).toEpochMilli()
            } catch (_: Exception) { }
        }

        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy",
            "MMM dd, yyyy hh:mm:ss a",
            "MMM dd, yyyy"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = if (pattern.endsWith("'Z'")) TimeZone.getTimeZone("UTC") else TimeZone.getDefault()
                }
                val d = sdf.parse(value)
                if (d != null) return d.time
            } catch (_: Exception) { }
        }
        return null
    }

    private fun generateAvatarColor(id: String, seed: String): String {
        val colors = listOf("#2563EB", "#7C3AED", "#059669", "#D97706", "#DC2626", "#0284C7", "#4F46E5", "#0D9488")
        val hash = (id + seed).hashCode()
        val index = kotlin.math.abs(hash) % colors.size
        return colors[index]
    }

    /**
     * Exhaustively parses phone / mobile and country code from any Firestore document schema.
     * Searches top-level keys, nested profile/contactInfo containers, numbers, strings, and maps.
     */
    private fun extractMobileAndCountryCode(data: Map<String, Any?>): Pair<String, String> {
        var rawMobile = ""
        var rawCountryCode = ""

        val phoneKeyCandidates = listOf(
            "mobilenumber", "mobile_number", "mobile", "mobileno", "mobile_no", "mob",
            "phonenumber", "phone_number", "phone", "phoneno", "phone_no",
            "contactnumber", "contact_number", "contact", "contactno", "contact_no",
            "userphone", "user_phone", "usermobile", "user_mobile", "usernumber", "user_number",
            "cell", "cellphone", "cell_phone", "cellnumber", "telephone", "tel",
            "whatsapp", "whatsappnumber", "whatsapp_number", "whatsappno", "whatsapp_no"
        )

        val countryCodeKeyCandidates = listOf(
            "countrycode", "country_code", "dialcode", "dial_code", "countrydialcode",
            "dialingcode", "isdcode", "isd_code", "code", "stdcode", "std_code"
        )

        fun findByCaseInsensitiveKey(map: Map<*, *>, candidates: List<String>): Any? {
            for ((k, v) in map) {
                val keyStr = k?.toString()?.lowercase()?.trim() ?: continue
                if (candidates.contains(keyStr)) {
                    if (v != null && v.toString().isNotBlank()) return v
                }
            }
            return null
        }

        // 1. Check top-level
        val foundPhone = findByCaseInsensitiveKey(data, phoneKeyCandidates)
        val foundCode = findByCaseInsensitiveKey(data, countryCodeKeyCandidates)

        if (foundCode != null) {
            rawCountryCode = foundCode.toString().trim()
        }

        if (foundPhone is Map<*, *>) {
            val nestedNumber = findByCaseInsensitiveKey(foundPhone, phoneKeyCandidates)
            val nestedCode = findByCaseInsensitiveKey(foundPhone, countryCodeKeyCandidates)
            if (nestedNumber != null) rawMobile = nestedNumber.toString().trim()
            if (nestedCode != null && rawCountryCode.isBlank()) rawCountryCode = nestedCode.toString().trim()
        } else if (foundPhone != null) {
            rawMobile = when (foundPhone) {
                is Number -> foundPhone.toLong().toString()
                else -> foundPhone.toString().trim()
            }
        }

        // 2. Check nested containers
        if (rawMobile.isBlank()) {
            val nestedContainers = listOf(
                "profile", "contactinfo", "contact_info", "contact", "details",
                "personaldetails", "personal_details", "user", "userdata", "user_data",
                "data", "account", "info", "billing"
            )
            for ((k, v) in data) {
                val keyStr = k.lowercase().trim()
                if (nestedContainers.contains(keyStr) && v is Map<*, *>) {
                    val nestedPhone = findByCaseInsensitiveKey(v, phoneKeyCandidates)
                    if (nestedPhone != null) {
                        rawMobile = when (nestedPhone) {
                            is Number -> nestedPhone.toLong().toString()
                            is Map<*, *> -> {
                                val innerNum = findByCaseInsensitiveKey(nestedPhone, phoneKeyCandidates)
                                innerNum?.toString()?.trim() ?: ""
                            }
                            else -> nestedPhone.toString().trim()
                        }
                    }
                    if (rawCountryCode.isBlank()) {
                        val nestedCode = findByCaseInsensitiveKey(v, countryCodeKeyCandidates)
                        if (nestedCode != null) rawCountryCode = nestedCode.toString().trim()
                    }
                    if (rawMobile.isNotBlank()) break
                }
            }
        }

        // 3. Fuzzy search any key containing 'phone', 'mobile', 'contact', 'cell'
        if (rawMobile.isBlank()) {
            for ((k, v) in data) {
                val keyStr = k.lowercase()
                if (keyStr.contains("mobile") || keyStr.contains("phone") || keyStr.contains("contact") || keyStr.contains("cell")) {
                    if (v is Number) {
                        val digits = v.toLong().toString()
                        if (digits.length >= 7) {
                            rawMobile = digits
                            break
                        }
                    } else if (v is String && v.isNotBlank() && !v.contains("@") && !v.startsWith("http")) {
                        val digits = v.filter { it.isDigit() }
                        if (digits.length >= 7) {
                            rawMobile = v.trim()
                            break
                        }
                    }
                }
            }
        }

        // 4. Country Code extraction / formatting
        if (rawMobile.startsWith("+")) {
            if (rawCountryCode.isBlank()) {
                val match = Regex("^(\\+[0-9]{1,3})\\s*(.*)$").find(rawMobile)
                if (match != null) {
                    rawCountryCode = match.groupValues[1]
                    rawMobile = match.groupValues[2].trim()
                }
            } else {
                val cleanCode = if (rawCountryCode.startsWith("+")) rawCountryCode else "+$rawCountryCode"
                val codeDigits = cleanCode.filter { it.isDigit() }
                if (codeDigits.isNotEmpty() && rawMobile.startsWith("+$codeDigits")) {
                    rawMobile = rawMobile.removePrefix("+$codeDigits").trimStart('-', ' ', '.')
                }
            }
        } else if (rawCountryCode.isNotBlank() && !rawCountryCode.startsWith("+") && rawCountryCode.any { it.isDigit() }) {
            rawCountryCode = "+$rawCountryCode"
        }

        return Pair(rawMobile, rawCountryCode)
    }

    /**
     * Switch the active Firestore collection (e.g. 'users', 'Users', 'customers')
     */
    fun setCollection(collectionName: String) {
        val cleanName = collectionName.trim()
        if (cleanName.isBlank() || cleanName == _activeCollection.value) return

        _activeCollection.value = cleanName
        context?.let { ctx ->
            try {
                ctx.getSharedPreferences("firebase_hub_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("collection_name", cleanName)
                    .apply()
            } catch (e: Exception) {
                Log.w(tag, "Failed to save collection preference: ${e.message}")
            }
        }

        val db = getOrInitFirestore()
        if (db != null) {
            listenToCollection(db, cleanName)
        }
    }

    /**
     * Configure Firebase Project manually with Project ID
     */
    fun configureFirebaseProject(
        projectId: String,
        apiKey: String? = null,
        appId: String? = null,
        collection: String? = null
    ) {
        val cleanProjectId = projectId.trim()
        if (cleanProjectId.isBlank()) return

        appContext?.let { ctx ->
            try {
                ctx.getSharedPreferences("firebase_hub_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("project_id", cleanProjectId)
                    .apply {
                        if (!apiKey.isNullOrBlank()) putString("api_key", apiKey.trim())
                        if (!appId.isNullOrBlank()) putString("app_id", appId.trim())
                        if (!collection.isNullOrBlank()) putString("collection_name", collection.trim())
                    }
                    .apply()

                val options = FirebaseOptions.Builder()
                    .setProjectId(cleanProjectId)
                    .setApiKey(apiKey?.trim()?.ifBlank { "AIzaSyDummyKeyForFirestore" } ?: "AIzaSyDummyKeyForFirestore")
                    .setApplicationId(appId?.trim()?.ifBlank { "1:1234567890:android:abcdef" } ?: "1:1234567890:android:abcdef")
                    .build()

                // Re-initialize app with these options
                try {
                    val existing = FirebaseApp.getApps(ctx).find { it.name == FirebaseApp.DEFAULT_APP_NAME }
                    if (existing != null) {
                        existing.delete()
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Clean previous app: ${e.message}")
                }

                val app = FirebaseApp.initializeApp(ctx, options)
                _firebaseProjectId.value = cleanProjectId
                firestore = FirebaseFirestore.getInstance(app)

                if (!collection.isNullOrBlank()) {
                    _activeCollection.value = collection.trim()
                }

                firestore?.let { db ->
                    listenToCollection(db, _activeCollection.value)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to configure Firebase project: ${e.message}")
                _errorMessage.value = "Connection error: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    /**
     * Activates a user account (e.g. from pending or blocked).
     * Sets status = "active", stopapp = false, stopapp_reason = null, updated_at = Timestamp.now()
     * Allows the user app to open directly to the camera on next launch/login.
     */
    suspend fun activateUser(userId: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val currentList = _usersFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.userId == userId }
            if (index != -1) {
                currentList[index] = currentList[index].copy(
                    status = "active",
                    stopapp = false,
                    stopappReason = null,
                    updatedAt = now
                )
                _usersFlow.value = currentList
            }

            val db = getOrInitFirestore()
            db?.collection(_activeCollection.value)?.document(userId)
                ?.update(
                    mapOf(
                        "status" to "active",
                        "stopapp" to false,
                        "stopapp_reason" to null,
                        "updated_at" to com.google.firebase.Timestamp.now()
                    )
                )
                ?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error activating user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Blocks a user account.
     * Sets status = "blocked", updated_at = Timestamp.now()
     */
    suspend fun blockUser(userId: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val currentList = _usersFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.userId == userId }
            if (index != -1) {
                currentList[index] = currentList[index].copy(
                    status = "blocked",
                    updatedAt = now
                )
                _usersFlow.value = currentList
            }

            val db = getOrInitFirestore()
            db?.collection(_activeCollection.value)?.document(userId)
                ?.update(
                    mapOf(
                        "status" to "blocked",
                        "updated_at" to com.google.firebase.Timestamp.now()
                    )
                )
                ?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error blocking user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Stop app for one user immediately (Kill Switch).
     * Sets stopapp = true, stopapp_reason = reason, updated_at = Timestamp.now()
     * The user app has a live Firestore listener: block dialog appears immediately even while app is open!
     */
    suspend fun stopAppForUser(userId: String, reason: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val currentList = _usersFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.userId == userId }
            if (index != -1) {
                currentList[index] = currentList[index].copy(
                    stopapp = true,
                    stopappReason = reason,
                    updatedAt = now
                )
                _usersFlow.value = currentList
            }

            val db = getOrInitFirestore()
            db?.collection(_activeCollection.value)?.document(userId)
                ?.update(
                    mapOf(
                        "stopapp" to true,
                        "stopapp_reason" to reason,
                        "updated_at" to com.google.firebase.Timestamp.now()
                    )
                )
                ?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error stopping app for user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Restores access after Stop App.
     * Sets status = "active", stopapp = false, stopapp_reason = null, updated_at = Timestamp.now()
     */
    suspend fun restoreUserAccess(userId: String): Result<Unit> {
        return activateUser(userId)
    }

    suspend fun updateUserStatus(userId: String, newStatus: String): Result<Unit> {
        return when (newStatus.lowercase().trim()) {
            "active" -> activateUser(userId)
            "blocked" -> blockUser(userId)
            else -> {
                try {
                    val now = System.currentTimeMillis()
                    val currentList = _usersFlow.value.toMutableList()
                    val index = currentList.indexOfFirst { it.userId == userId }
                    if (index != -1) {
                        currentList[index] = currentList[index].copy(
                            status = newStatus,
                            updatedAt = now
                        )
                        _usersFlow.value = currentList
                    }

                    val db = getOrInitFirestore()
                    db?.collection(_activeCollection.value)?.document(userId)
                        ?.update(
                            mapOf(
                                "status" to newStatus,
                                "updated_at" to com.google.firebase.Timestamp.now()
                            )
                        )
                        ?.await()

                    Result.success(Unit)
                } catch (e: Exception) {
                    Log.e(tag, "Error updating user status in Firestore: ${e.message}")
                    Result.failure(e)
                }
            }
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
            val currentList = _usersFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.userId == updatedUser.userId }
            if (index != -1) {
                currentList[index] = updatedUser
                _usersFlow.value = currentList
            } else {
                currentList.add(0, updatedUser)
                _usersFlow.value = currentList
            }

            val db = getOrInitFirestore()
            val docRef = db?.collection(_activeCollection.value)?.document(updatedUser.userId)

            // 1. Fetch the existing document to strictly preserve existing schema keys and NOT create extra redundant fields
            val existingSnapshot = docRef?.get()?.await()
            val existingData = existingSnapshot?.data ?: emptyMap<String, Any?>()

            val updates = mutableMapOf<String, Any?>()

            // 2. Business / Customer Name - update existing field ONLY
            if (existingData.containsKey("business_name")) {
                updates["business_name"] = updatedUser.businessName.ifBlank { updatedUser.customerName }
            } else if (existingData.containsKey("businessName")) {
                updates["businessName"] = updatedUser.businessName.ifBlank { updatedUser.customerName }
            } else if (existingData.containsKey("customerName")) {
                updates["customerName"] = updatedUser.customerName
            } else if (existingData.containsKey("name")) {
                updates["name"] = updatedUser.customerName
            } else {
                updates["business_name"] = updatedUser.businessName.ifBlank { updatedUser.customerName }
            }

            // 3. Mobile number - update existing field ONLY (as it is without extra logic)
            if (existingData.containsKey("mobile_number")) {
                updates["mobile_number"] = updatedUser.mobileNumber
            } else if (existingData.containsKey("mobileNumber")) {
                updates["mobileNumber"] = updatedUser.mobileNumber
            } else if (existingData.containsKey("mobile")) {
                updates["mobile"] = updatedUser.mobileNumber
            } else if (existingData.containsKey("phone")) {
                updates["phone"] = updatedUser.mobileNumber
            } else if (existingData.containsKey("phoneNumber")) {
                updates["phoneNumber"] = updatedUser.mobileNumber
            } else {
                updates["mobile_number"] = updatedUser.mobileNumber
            }

            // 4. Address fields - update only existing keys
            if (existingData.containsKey("address")) updates["address"] = updatedUser.address
            if (existingData.containsKey("city")) updates["city"] = updatedUser.city
            if (existingData.containsKey("state")) updates["state"] = updatedUser.state
            if (existingData.containsKey("country")) updates["country"] = updatedUser.country
            if (existingData.containsKey("pincode")) updates["pincode"] = updatedUser.pincode
            else if (existingData.containsKey("pinCode")) updates["pinCode"] = updatedUser.pincode
            else if (existingData.containsKey("postal_code")) updates["postal_code"] = updatedUser.pincode

            // 5. Status
            if (existingData.containsKey("status")) {
                updates["status"] = updatedUser.status
            }

            // 6. Updated timestamp - preserve existing key name and format
            if (existingData.containsKey("updated_at")) {
                updates["updated_at"] = com.google.firebase.Timestamp.now()
            } else if (existingData.containsKey("updatedAt")) {
                val existingVal = existingData["updatedAt"]
                if (existingVal is com.google.firebase.Timestamp) {
                    updates["updatedAt"] = com.google.firebase.Timestamp.now()
                } else {
                    updates["updatedAt"] = System.currentTimeMillis()
                }
            } else {
                updates["updated_at"] = com.google.firebase.Timestamp.now()
            }

            // Strictly update existing document keys without adding extra fields (and without touching email)
            try {
                docRef?.update(updates)?.await()
            } catch (e: Exception) {
                // If doc did not exist yet, fallback to merge
                docRef?.set(updates, SetOptions.merge())?.await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error updating user in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val targetUser = _usersFlow.value.find { it.userId == userId || it.firebaseUid == userId }
            val currentList = _usersFlow.value.toMutableList()
            currentList.removeAll { it.userId == userId || it.firebaseUid == userId }
            _usersFlow.value = currentList

            val db = getOrInitFirestore()
            val docId = targetUser?.userId ?: userId
            val firebaseUid = targetUser?.firebaseUid?.ifBlank { docId } ?: docId
            val userEmail = targetUser?.email ?: ""

            val docRef = db?.collection(_activeCollection.value)?.document(docId)

            // 1. Mark soft-delete in Firestore first (sets deleted_at, status = "deleted", stopapp = true)
            val softDeletePayload = mapOf(
                "deleted_at" to com.google.firebase.Timestamp.now(),
                "status" to "deleted",
                "stopapp" to true,
                "stopapp_reason" to "Account deleted by Administrator",
                "updated_at" to com.google.firebase.Timestamp.now()
            )
            try {
                docRef?.update(softDeletePayload)?.await()
            } catch (e: Exception) {
                Log.w(tag, "Soft-delete update attempt: ${e.message}")
            }

            // 2. Delete document from active Firestore collection
            try {
                docRef?.delete()?.await()
                if (firebaseUid != docId) {
                    db?.collection(_activeCollection.value)?.document(firebaseUid)?.delete()?.await()
                }
            } catch (e: Exception) {
                Log.w(tag, "Active collection document delete: ${e.message}")
            }

            // 3. Queue for Firebase Authentication Deletion & Serverless Event Trigger across standard collections
            val deletionPayload = mapOf(
                "uid" to firebaseUid,
                "firebase_uid" to firebaseUid,
                "email" to userEmail,
                "deleted_at" to com.google.firebase.Timestamp.now(),
                "status" to "deleted",
                "action" to "delete_user",
                "source" to "admin_app"
            )

            try {
                db?.collection("deleted_users")?.document(firebaseUid)?.set(deletionPayload)?.await()
            } catch (e: Exception) {
                Log.w(tag, "Could not write to deleted_users trigger collection: ${e.message}")
            }

            try {
                db?.collection("auth_deletion_queue")?.document(firebaseUid)?.set(deletionPayload)?.await()
            } catch (e: Exception) {
                Log.w(tag, "Could not write to auth_deletion_queue collection: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error deleting user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val finalUserId = if (user.userId.isBlank()) {
                "usr_" + UUID.randomUUID().toString().take(8)
            } else user.userId

            val newUser = user.copy(
                userId = finalUserId,
                createdAt = now,
                updatedAt = now
            )

            val currentList = _usersFlow.value.toMutableList()
            currentList.add(0, newUser)
            _usersFlow.value = currentList

            val db = getOrInitFirestore()
            db?.collection(_activeCollection.value)?.document(finalUserId)
                ?.set(newUser)
                ?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error creating user in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun refresh(): Result<Unit> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val db = getOrInitFirestore()
            if (db != null) {
                if (snapshotListener == null) {
                    listenToCollection(db, _activeCollection.value)
                }
                val snapshots = db.collection(_activeCollection.value).get().await()
                if (!snapshots.isEmpty) {
                    val actualUsers = snapshots.documents.map { doc ->
                        mapDocumentToUser(doc)
                    }.filter { it.deletedAt == null && it.status != "deleted" }
                    _usersFlow.value = actualUsers
                    _isFirebaseOnline.value = true
                } else {
                    _usersFlow.value = emptyList()
                    _isFirebaseOnline.value = true
                }
            } else {
                _usersFlow.value = emptyList()
            }
            _isLoading.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Refresh error: ${e.message}")
            _isLoading.value = false
            _errorMessage.value = "Unable to fetch from Firebase: ${e.localizedMessage ?: e.message}"
            Result.failure(e)
        }
    }

    /**
     * Signs in anonymously with Firebase Auth to authorize requests if security rules require request.auth != null
     */
    suspend fun signInAnonymously(): Result<String?> {
        return try {
            val app = runCatching { FirebaseApp.getInstance() }.getOrNull()
            val auth = if (app != null) FirebaseAuth.getInstance(app) else FirebaseAuth.getInstance()
            val user = auth.currentUser
            if (user != null) {
                Log.i(tag, "Firebase Auth already signed in: ${user.uid}")
                Result.success(user.uid)
            } else {
                val authResult = auth.signInAnonymously().await()
                val uid = authResult.user?.uid
                Log.i(tag, "Signed in anonymously with Firebase Auth UID: $uid")
                _errorMessage.value = null
                // Trigger a refresh with the authenticated session
                refresh()
                Result.success(uid)
            }
        } catch (e: Exception) {
            Log.e(tag, "Anonymous sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun getCurrentAuthUid(): String? {
        return try {
            val app = runCatching { FirebaseApp.getInstance() }.getOrNull()
            val auth = if (app != null) FirebaseAuth.getInstance(app) else FirebaseAuth.getInstance()
            auth.currentUser?.uid
        } catch (_: Exception) {
            null
        }
    }
}
