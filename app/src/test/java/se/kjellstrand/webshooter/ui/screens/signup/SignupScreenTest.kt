package se.kjellstrand.webshooter.ui.screens.signup

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SignupScreenTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/ui/screens/signup/SignupScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `error field is not accessed with non-null assertion`() {
        assertTrue("Source file should exist", sourceFile.exists())
        val source = sourceFile.readText()
        assertFalse(
            "uiState.error!! is unsafe — use safe call or let-binding instead",
            source.contains("uiState.error!!")
        )
    }

    @Test
    fun `error display uses safe call or let binding`() {
        val source = sourceFile.readText()
        val usesLetBinding = source.contains("uiState.error?.let")
        val usesSafeCall = source.contains("uiState.error?.")
        val usesOrEmpty = source.contains("uiState.error.orEmpty()")
        val usesElvis = source.contains("uiState.error ?: ")
        assertTrue(
            "Error should be accessed safely via ?. or let or orEmpty() or elvis",
            usesLetBinding || usesSafeCall || usesOrEmpty || usesElvis
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue("SignupScreen.kt should exist", sourceFile.exists())
    }

    @Test
    fun `error null check exists`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should check error for null before displaying",
            source.contains("uiState.error != null") || source.contains("uiState.error?.let")
        )
    }

    @Test
    fun `error text uses error color`() {
        val source = sourceFile.readText()
        assertTrue(
            "Error text should use error color scheme",
            source.contains("colorScheme.error")
        )
    }
}
