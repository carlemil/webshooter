package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ContentDescriptionTest {

    private val loginScreen = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/login/LoginScreen.kt")
    private val settingsScreen = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/settings/SettingsScreen.kt")
    private val signupScreen = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/signup/SignupScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `LoginScreen password toggle has non-null content description`() {
        val source = loginScreen.readText()
        // Find the password visibility icon section
        val iconSection = source.substringAfter("passwordVisible")
        assertFalse(
            "Password visibility toggle in LoginScreen should not have contentDescription = null",
            iconSection.contains("contentDescription = null")
        )
    }

    @Test
    fun `SettingsScreen password toggle has non-null content description`() {
        val source = settingsScreen.readText()
        // PasswordField composable contains the visibility toggle
        val passwordFieldSection = source.substringAfter("fun PasswordField")
        assertFalse(
            "Password visibility toggle in SettingsScreen should not have contentDescription = null",
            passwordFieldSection.contains("contentDescription = null")
        )
    }

    @Test
    fun `SignupScreen back button uses stringResource not hardcoded Back`() {
        val source = signupScreen.readText()
        assertFalse(
            "Back button should use stringResource instead of hardcoded 'Back'",
            source.contains("contentDescription = \"Back\"")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `LoginScreen exists`() {
        assertTrue(loginScreen.exists())
    }

    @Test
    fun `SettingsScreen exists`() {
        assertTrue(settingsScreen.exists())
    }

    @Test
    fun `SignupScreen exists`() {
        assertTrue(signupScreen.exists())
    }

    @Test
    fun `LoginScreen has password visibility toggle`() {
        val source = loginScreen.readText()
        assertTrue(source.contains("passwordVisible"))
    }

    @Test
    fun `SignupScreen has back navigation icon`() {
        val source = signupScreen.readText()
        assertTrue(source.contains("ArrowBack"))
    }
}
