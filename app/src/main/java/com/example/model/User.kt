package com.example.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Standard Status constants for User accounts
 */
enum class UserStatus(val value: String, val label: String) {
    ACTIVE("active", "Active"),
    PENDING("pending", "Pending"),
    BLOCKED("blocked", "Blocked");

    companion object {
        fun fromString(statusStr: String?): UserStatus {
            return when (statusStr?.lowercase()?.trim()) {
                "active" -> ACTIVE
                "pending" -> PENDING
                "blocked" -> BLOCKED
                else -> ACTIVE
            }
        }
    }
}

/**
 * Clean User data model representing a customer entity in the backend / Firebase Firestore.
 */
@IgnoreExtraProperties
data class User(
    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("firebase_uid") @set:PropertyName("firebase_uid")
    var firebaseUid: String = "",

    @get:PropertyName("customerName") @set:PropertyName("customerName")
    var customerName: String = "",

    @get:PropertyName("businessName") @set:PropertyName("businessName")
    var businessName: String = "",

    @get:PropertyName("email") @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("countryCode") @set:PropertyName("countryCode")
    var countryCode: String = "",

    @get:PropertyName("mobileNumber") @set:PropertyName("mobileNumber")
    var mobileNumber: String = "",

    @get:PropertyName("company") @set:PropertyName("company")
    var company: String = "",

    @get:PropertyName("address") @set:PropertyName("address")
    var address: String = "",

    @get:PropertyName("country") @set:PropertyName("country")
    var country: String = "",

    @get:PropertyName("state") @set:PropertyName("state")
    var state: String = "",

    @get:PropertyName("city") @set:PropertyName("city")
    var city: String = "",

    @get:PropertyName("pincode") @set:PropertyName("pincode")
    var pincode: String = "",

    @get:PropertyName("phoneVerified") @set:PropertyName("phoneVerified")
    var phoneVerified: Boolean = false,

    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = "active", // "active", "pending", "blocked"

    @get:PropertyName("stopapp") @set:PropertyName("stopapp")
    var stopapp: Boolean = false,

    @get:PropertyName("stopappReason") @set:PropertyName("stopappReason")
    var stopappReason: String? = null,

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Long = System.currentTimeMillis(),

    @get:PropertyName("lastLoginAt") @set:PropertyName("lastLoginAt")
    var lastLoginAt: Long? = null,

    @get:PropertyName("deletedAt") @set:PropertyName("deletedAt")
    var deletedAt: Long? = null,

    @get:PropertyName("avatarColorHex") @set:PropertyName("avatarColorHex")
    var avatarColorHex: String = "#2563EB"
) {
    // Enum representation of status
    val userStatus: UserStatus
        get() = UserStatus.fromString(status)

    val isPending: Boolean
        get() = userStatus == UserStatus.PENDING

    val isBlocked: Boolean
        get() = userStatus == UserStatus.BLOCKED

    val isActive: Boolean
        get() = userStatus == UserStatus.ACTIVE

    // Display business name or customer name
    val displayName: String
        get() = when {
            businessName.isNotBlank() -> businessName
            customerName.isNotBlank() -> customerName
            company.isNotBlank() -> company
            email.isNotBlank() -> email.substringBefore("@")
            else -> "User ${userId.take(6)}"
        }

    // Show clean, full mobile number gracefully with country code if present
    val fullMobileNumber: String
        get() {
            val num = mobileNumber.trim()
            val code = countryCode.trim()
            return when {
                num.isBlank() -> "Not Available"
                num.startsWith("+") -> num
                code.isNotBlank() -> {
                    val formattedCode = if (code.startsWith("+")) code else "+$code"
                    if (num.startsWith(formattedCode)) num else "$formattedCode$num"
                }
                else -> num
            }
        }

    // Helper for avatar initials
    val initials: String
        get() {
            if (customerName.isBlank()) {
                return email.take(2).uppercase().ifBlank { "US" }
            }
            val parts = customerName.trim().split("\\s+".toRegex())
            return if (parts.size >= 2) {
                "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
            } else {
                customerName.take(2).uppercase()
            }
        }

    // Location display helper
    val locationDisplay: String
        get() {
            val c = city.trim()
            val cntry = country.trim()
            return when {
                c.isNotEmpty() && cntry.isNotEmpty() -> "$c, $cntry"
                c.isNotEmpty() -> c
                cntry.isNotEmpty() -> cntry
                else -> "Not Available"
            }
        }
}
