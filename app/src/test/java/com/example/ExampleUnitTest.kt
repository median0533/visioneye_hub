package com.example

import com.example.util.AuthManager
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying AuthManager and login authentication.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun authManager_validCredentials_succeeds() {
        val result = AuthManager.authenticate("visioneye360", "Alpoemkavi$1937")
        assertTrue("Fixed credentials should succeed", result)
    }

    @Test
    fun authManager_validCredentialsWithWhitespace_succeeds() {
        val result = AuthManager.authenticate(" visioneye360 ", "Alpoemkavi$1937")
        assertTrue("Admin ID with whitespace should succeed", result)
    }

    @Test
    fun authManager_invalidId_fails() {
        val result = AuthManager.authenticate("wrong_user", "Alpoemkavi$1937")
        assertFalse("Wrong ID must fail", result)
    }

    @Test
    fun authManager_invalidPassword_fails() {
        val result = AuthManager.authenticate("visioneye360", "wrong_pass")
        assertFalse("Wrong password must fail", result)
    }
}
