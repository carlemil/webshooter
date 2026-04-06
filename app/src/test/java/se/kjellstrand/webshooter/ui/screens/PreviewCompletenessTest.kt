package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PreviewCompletenessTest {

    private val screenBase = "src/main/java/se/kjellstrand/webshooter/ui/screens"

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    // Results Screen
    @Test
    fun `ResultsScreen should have loading preview`() {
        val source = File("$screenBase/results/ResultsScreen.kt").readText()
        assertTrue(source.contains("ResultsScreenLoadingPreview"))
    }

    @Test
    fun `ResultsScreen should have empty preview`() {
        val source = File("$screenBase/results/ResultsScreen.kt").readText()
        assertTrue(source.contains("ResultsScreenEmptyPreview"))
    }

    @Test
    fun `ResultsViewModelMock should accept UiState constructor parameter`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock")
        val hasUiStateParam = clazz.declaredConstructors.any { c ->
            c.parameterTypes.any { it.simpleName == "ResultsUiState" }
        }
        assertTrue(hasUiStateParam)
    }

    // Club Screen
    @Test
    fun `ClubScreen should have loaded preview`() {
        val source = File("$screenBase/club/ClubScreen.kt").readText()
        assertTrue(source.contains("ClubScreenPreview"))
    }

    @Test
    fun `ClubScreen should have loading preview`() {
        val source = File("$screenBase/club/ClubScreen.kt").readText()
        assertTrue(source.contains("ClubScreenLoadingPreview"))
    }

    @Test
    fun `ClubScreen should have admins tab preview`() {
        val source = File("$screenBase/club/ClubScreen.kt").readText()
        assertTrue(source.contains("ClubScreenAdminsPreview"))
    }

    @Test
    fun `ClubScreen should have error preview`() {
        val source = File("$screenBase/club/ClubScreen.kt").readText()
        assertTrue(source.contains("ClubScreenErrorPreview"))
    }

    // MyResults Screen
    @Test
    fun `MyResultsScreen should have loaded preview`() {
        val source = File("$screenBase/myresults/MyResultsScreen.kt").readText()
        assertTrue(source.contains("MyEntriesScreenPreview"))
    }

    @Test
    fun `MyResultsScreen should have loading preview`() {
        val source = File("$screenBase/myresults/MyResultsScreen.kt").readText()
        assertTrue(source.contains("MyEntriesScreenLoadingPreview"))
    }

    // Patrols Screen
    @Test
    fun `PatrolsScreen should have loaded preview`() {
        val source = File("$screenBase/patrols/PatrolsScreen.kt").readText()
        assertTrue(source.contains("PatrolsScreenPreview"))
    }

    @Test
    fun `PatrolsScreen should have loading preview`() {
        val source = File("$screenBase/patrols/PatrolsScreen.kt").readText()
        assertTrue(source.contains("PatrolsScreenLoadingPreview"))
    }

    // Settings Screen
    @Test
    fun `SettingsScreen should have profile preview`() {
        val source = File("$screenBase/settings/SettingsScreen.kt").readText()
        assertTrue(source.contains("SettingsScreenPreview"))
    }

    @Test
    fun `SettingsScreen should have edit mode preview`() {
        val source = File("$screenBase/settings/SettingsScreen.kt").readText()
        assertTrue(source.contains("SettingsScreenEditPreview"))
    }

    @Test
    fun `SettingsScreen should have password tab preview`() {
        val source = File("$screenBase/settings/SettingsScreen.kt").readText()
        assertTrue(source.contains("SettingsScreenPasswordPreview"))
    }

    // ShooterResult Screen
    @Test
    fun `ShooterResultScreen should have loaded preview`() {
        val source = File("$screenBase/shooterresult/ShooterResultScreen.kt").readText()
        assertTrue(source.contains("ShooterResultScreenPreview"))
    }

    @Test
    fun `ShooterResultScreen should have loading preview`() {
        val source = File("$screenBase/shooterresult/ShooterResultScreen.kt").readText()
        assertTrue(source.contains("ShooterResultScreenLoadingPreview"))
    }

    // Signup Screen
    @Test
    fun `SignupScreen should have default preview`() {
        val source = File("$screenBase/signup/SignupScreen.kt").readText()
        assertTrue(source.contains("SignupScreenPreview"))
    }

    @Test
    fun `SignupScreen should have error preview`() {
        val source = File("$screenBase/signup/SignupScreen.kt").readText()
        assertTrue(source.contains("SignupScreenErrorPreview"))
    }

    // Signups Screen
    @Test
    fun `SignupsScreen should have loaded preview`() {
        val source = File("$screenBase/signups/SignupsScreen.kt").readText()
        assertTrue(source.contains("SignupsScreenPreview"))
    }

    @Test
    fun `SignupsScreen should have loading preview`() {
        val source = File("$screenBase/signups/SignupsScreen.kt").readText()
        assertTrue(source.contains("SignupsScreenLoadingPreview"))
    }

    // Teams Screen
    @Test
    fun `TeamsScreen should have loaded preview`() {
        val source = File("$screenBase/teams/TeamsScreen.kt").readText()
        assertTrue(source.contains("TeamsScreenPreview"))
    }

    @Test
    fun `TeamsScreen should have loading preview`() {
        val source = File("$screenBase/teams/TeamsScreen.kt").readText()
        assertTrue(source.contains("TeamsScreenLoadingPreview"))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `all screen files exist`() {
        listOf("results", "club", "myresults", "patrols", "settings", "shooterresult", "signup", "signups", "teams").forEach { screen ->
            val dir = File("$screenBase/$screen")
            assertTrue("$screen directory should exist", dir.exists())
        }
    }

    @Test
    fun `all screens have at least one Preview annotation`() {
        listOf(
            "results/ResultsScreen.kt", "club/ClubScreen.kt", "myresults/MyResultsScreen.kt",
            "patrols/PatrolsScreen.kt", "settings/SettingsScreen.kt", "shooterresult/ShooterResultScreen.kt",
            "signup/SignupScreen.kt", "signups/SignupsScreen.kt", "teams/TeamsScreen.kt"
        ).forEach { path ->
            val source = File("$screenBase/$path").readText()
            assertTrue("$path should have @Preview", source.contains("@Preview"))
        }
    }
}
