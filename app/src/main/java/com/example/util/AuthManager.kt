package com.example.util

import android.content.Context

/**
 * Manages administrator authentication with fixed credentials:
 * ID: visioneye360
 * Pass: Alpoemkavi$1937
 */
object AuthManager {
    private const val PREFS_NAME = "visioneye_auth_prefs"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_ADMIN_ID = "key_admin_id"

    const val FIXED_ADMIN_ID = "visioneye360"
    const val FIXED_ADMIN_PASS = "Alpoemkavi$1937"

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun authenticate(id: String, pass: String): Boolean {
        return id.trim() == FIXED_ADMIN_ID && pass == FIXED_ADMIN_PASS
    }

    fun setLoggedIn(context: Context, id: String, rememberMe: Boolean = true) {
        if (rememberMe) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_ADMIN_ID, id.trim())
                .apply()
        }
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
