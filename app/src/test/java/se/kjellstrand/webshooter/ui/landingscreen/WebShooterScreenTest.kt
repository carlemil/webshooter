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

    @Test
    fun `strings define four menu group section strings`() {
        val strings = File("../shared/src/commonMain/composeResources/values/strings.xml").readText()
        assertTrue(
            "strings.xml must define menu_group_competitions = Tävlingar",
            strings.contains("<string name=\"menu_group_competitions\">Tävlingar</string>")
        )
        assertTrue(
            "strings.xml must define menu_group_stats = Statistik",
            strings.contains("<string name=\"menu_group_stats\">Statistik</string>")
        )
        assertTrue(
            "strings.xml must define menu_group_club = Förening",
            strings.contains("<string name=\"menu_group_club\">Förening</string>")
        )
        assertTrue(
            "strings.xml must define menu_group_settings = Inställningar",
            strings.contains("<string name=\"menu_group_settings\">Inställningar</string>")
        )
    }

    @Test
    fun `chart screen names are renamed to shorter Swedish labels`() {
        val strings = File("../shared/src/commonMain/composeResources/values/strings.xml").readText()
        assertTrue(
            "web_shooter_charts must now be 'Resultattrender'",
            strings.contains("<string name=\"web_shooter_charts\">Resultattrender</string>")
        )
        assertTrue(
            "web_shooter_series_points must now be 'Serieresultat'",
            strings.contains("<string name=\"web_shooter_series_points\">Serieresultat</string>")
        )
    }

    @Test
    fun `drawer renders section headers for all four groups`() {
        val source = sourceFile.readText()
        assertTrue(source.contains("Res.string.menu_group_competitions"))
        assertTrue(source.contains("Res.string.menu_group_stats"))
        assertTrue(source.contains("Res.string.menu_group_club"))
        assertTrue(source.contains("Res.string.menu_group_settings"))
    }

    @Test
    fun `drawer uses multiple HorizontalDividers to separate groups`() {
        val source = sourceFile.readText()
        val dividerCount = Regex("""HorizontalDivider\s*\(""").findAll(source).count()
        assertTrue(
            "Drawer should use at least 4 HorizontalDividers between groups; found $dividerCount",
            dividerCount >= 4
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
