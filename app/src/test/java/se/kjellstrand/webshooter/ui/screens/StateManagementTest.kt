package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class StateManagementTest {

    private val loginScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/login/LoginScreen.kt")
    private val webShooterScreen = File("src/main/java/se/kjellstrand/webshooter/ui/landingscreen/WebShooterScreen.kt")
    private val settingsScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/settings/SettingsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `passwordVisible uses rememberSaveable`() {
        val source = loginScreen.readText()
        assertTrue(
            "passwordVisible should use rememberSaveable for config change survival",
            source.contains("var passwordVisible by rememberSaveable")
        )
    }

    @Test
    fun `selectedRoute uses rememberSaveable`() {
        val source = webShooterScreen.readText()
        assertTrue(
            "selectedRoute should use rememberSaveable for config change survival",
            source.contains("var selectedRoute by rememberSaveable")
        )
    }

    @Test
    fun `showLogoutDialog uses rememberSaveable`() {
        val source = settingsScreen.readText()
        assertTrue(
            "showLogoutDialog should use rememberSaveable for config change survival",
            source.contains("var showLogoutDialog by rememberSaveable")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `login screen exists`() { assertTrue(loginScreen.exists()) }

    @Test
    fun `webshooter screen exists`() { assertTrue(webShooterScreen.exists()) }

    @Test
    fun `settings screen exists`() { assertTrue(settingsScreen.exists()) }
}
