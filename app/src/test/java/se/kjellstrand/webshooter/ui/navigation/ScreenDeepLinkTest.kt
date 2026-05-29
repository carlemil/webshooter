package se.kjellstrand.webshooter.ui.navigation

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ScreenDeepLinkTest {

    private val screenFile = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/navigation/Screen.kt")
    private val navHostFile = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/navigation/AppNavHost.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `Screen sealed class defines a deep link base URI`() {
        val source = screenFile.readText()
        assertTrue(
            "Screen should define a DEEP_LINK_BASE_URI constant for deep link generation",
            source.contains("DEEP_LINK_BASE_URI") || source.contains("deepLinkBaseUri")
        )
    }

    @Test
    fun `CompetitionResults screen has a deepLink property`() {
        val source = screenFile.readText()
        val competitionResultsSection = source.substringAfter("CompetitionResults")
            .substringBefore("object ")
        assertTrue(
            "CompetitionResults should define a deepLink URI pattern",
            competitionResultsSection.contains("deepLink") || competitionResultsSection.contains("DEEP_LINK")
        )
    }

    @Test
    fun `CompetitionSignup screen has a deepLink property`() {
        val source = screenFile.readText()
        val signupSection = source.substringAfter("object CompetitionSignup")
            .substringBefore("object ")
        assertTrue(
            "CompetitionSignup should define a deepLink URI pattern",
            signupSection.contains("deepLink") || signupSection.contains("DEEP_LINK")
        )
    }

    @Test
    fun `AppNavHost wires deepLinks for CompetitionResults`() {
        val source = navHostFile.readText()
        assertTrue(
            "AppNavHost composable for CompetitionResults should include deepLinks parameter",
            source.contains("deepLinks") && source.contains("CompetitionResults")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `Screen file exists`() {
        assertTrue("Screen.kt should exist", screenFile.exists())
    }

    @Test
    fun `AppNavHost file exists`() {
        assertTrue("AppNavHost.kt should exist", navHostFile.exists())
    }

    @Test
    fun `CompetitionResults screen has a route defined`() {
        val source = screenFile.readText()
        assertTrue(
            "CompetitionResults should have a route",
            source.contains("object CompetitionResults : Screen(")
        )
    }

    @Test
    fun `CompetitionResults createRoute function exists`() {
        val source = screenFile.readText()
        assertTrue(
            "CompetitionResults should have createRoute function",
            source.contains("fun createRoute(competitionId: Long, resultsType: String")
        )
    }

    @Test
    fun `AppNavHost has composable for CompetitionResults route`() {
        val source = navHostFile.readText()
        assertTrue(
            "AppNavHost should have a composable block for CompetitionResults",
            source.contains("Screen.CompetitionResults.route")
        )
    }
}
