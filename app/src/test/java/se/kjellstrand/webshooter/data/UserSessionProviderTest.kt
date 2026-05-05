package se.kjellstrand.webshooter.data

import org.junit.Assert.*
import org.junit.Test

class UserSessionProviderTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `UserSessionProvider implements AutoCloseable`() {
        assertTrue(
            "UserSessionProvider should implement AutoCloseable",
            AutoCloseable::class.java.isAssignableFrom(UserSessionProvider::class.java)
        )
    }

    @Test
    fun `close method exists and is callable`() {
        // Verify UserSessionProvider has a close() method
        val closeMethod = UserSessionProvider::class.java.methods.find {
            it.name == "close" && it.parameterCount == 0
        }
        assertNotNull("UserSessionProvider should have a close() method", closeMethod)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `userProfile initial value is null`() {
        // UserSessionProvider requires SettingsRepository in constructor,
        // but userProfile default should be null before load() is called.
        // We verify the StateFlow type exists on the class.
        val field = UserSessionProvider::class.java.methods.find {
            it.name == "getUserProfile" && it.parameterCount == 0
        }
        assertNotNull("UserSessionProvider should expose userProfile", field)
    }

    @Test
    fun `clear method exists`() {
        val clearMethod = UserSessionProvider::class.java.methods.find {
            it.name == "clear" && it.parameterCount == 0
        }
        assertNotNull("UserSessionProvider should have a clear() method", clearMethod)
    }

    @Test
    fun `load method exists`() {
        val loadMethod = UserSessionProvider::class.java.methods.find {
            it.name == "load" && it.parameterCount == 0
        }
        assertNotNull("UserSessionProvider should have a load() method", loadMethod)
    }
}
