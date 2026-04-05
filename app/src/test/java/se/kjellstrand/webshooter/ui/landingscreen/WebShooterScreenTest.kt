package se.kjellstrand.webshooter.ui.landingscreen

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class WebShooterScreenTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/ui/landingscreen/WebShooterScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `selectedRoute is derived from currentBackStackEntryAsState`() {
        val source = sourceFile.readText()
        assertTrue(
            "WebShooterScreen should use currentBackStackEntryAsState to derive navigation state",
            source.contains("currentBackStackEntryAsState")
        )
    }

    @Test
    fun `does not use manual mutableStateOf for selectedRoute`() {
        val source = sourceFile.readText()
        assertFalse(
            "WebShooterScreen should not use manual mutableStateOf for selectedRoute tracking",
            source.contains("mutableStateOf(Screen.CompetitionsList.route)")
        )
    }

    @Test
    fun `handles back press for non-default drawer items`() {
        val source = sourceFile.readText()
        assertTrue(
            "WebShooterScreen should handle back press to prevent desync (BackHandler or popBackStack)",
            source.contains("BackHandler") || source.contains("popBackStack")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue("WebShooterScreen.kt should exist", sourceFile.exists())
    }

    @Test
    fun `has drawer navigation with navigation items`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should have ModalNavigationDrawer",
            source.contains("ModalNavigationDrawer")
        )
    }

    @Test
    fun `renders CompetitionsScreen for competitions route`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should render CompetitionsScreen",
            source.contains("CompetitionsScreen(")
        )
    }

    @Test
    fun `renders SettingsScreen with onLoggedOut callback`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should render SettingsScreen with onLoggedOut",
            source.contains("SettingsScreen(") && source.contains("onLoggedOut")
        )
    }

    @Test
    fun `NavigationItem data class exists`() {
        val source = sourceFile.readText()
        assertTrue(
            "NavigationItem data class should exist",
            source.contains("data class NavigationItem")
        )
    }
}
