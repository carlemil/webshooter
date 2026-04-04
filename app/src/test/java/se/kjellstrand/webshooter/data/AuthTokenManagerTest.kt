package se.kjellstrand.webshooter.data

import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Modifier

class AuthTokenManagerTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `token field is volatile`() {
        val field = AuthTokenManager::class.java.getDeclaredField("token")
        assertTrue(
            "token field should be @Volatile for thread safety",
            Modifier.isVolatile(field.modifiers)
        )
    }

    @Test
    fun `refreshToken field is volatile`() {
        val field = AuthTokenManager::class.java.getDeclaredField("refreshToken")
        assertTrue(
            "refreshToken field should be @Volatile for thread safety",
            Modifier.isVolatile(field.modifiers)
        )
    }

    @Test
    fun `tokenExpiresAtMillis field is volatile`() {
        val field = AuthTokenManager::class.java.getDeclaredField("tokenExpiresAtMillis")
        assertTrue(
            "tokenExpiresAtMillis field should be @Volatile for thread safety",
            Modifier.isVolatile(field.modifiers)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `companion object has token field`() {
        val field = AuthTokenManager::class.java.getDeclaredField("token")
        assertNotNull("token field should exist", field)
    }

    @Test
    fun `companion object has refreshToken field`() {
        val field = AuthTokenManager::class.java.getDeclaredField("refreshToken")
        assertNotNull("refreshToken field should exist", field)
    }

    @Test
    fun `companion object has tokenExpiresAtMillis field`() {
        val field = AuthTokenManager::class.java.getDeclaredField("tokenExpiresAtMillis")
        assertNotNull("tokenExpiresAtMillis field should exist", field)
    }
}
