package se.kjellstrand.webshooter.ui.navigation

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AppNavHostSafetyTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/ui/navigation/AppNavHost.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `getBackStackEntry call is wrapped in try-catch or null safety`() {
        val source = sourceFile.readText()
        val getBackStackEntrySection = source.substringAfter("getBackStackEntry")
            .substringBefore("koinViewModel")
        // The getBackStackEntry call should be wrapped in a try-catch
        val hasTryCatch = source.contains("try") &&
            source.substringAfter("getBackStackEntry").substringBefore("}").let { after ->
                // Check if there's a catch block nearby
                source.indexOf("getBackStackEntry").let { idx ->
                    val surrounding = source.substring(
                        maxOf(0, idx - 200),
                        minOf(source.length, idx + 200)
                    )
                    surrounding.contains("catch") || surrounding.contains("runCatching") ||
                        surrounding.contains("getBackStackEntry(") && surrounding.contains("?.")
                }
            }
        assertTrue(
            "getBackStackEntry() should be wrapped in try-catch or use null safety to handle missing back stack entries",
            hasTryCatch
        )
    }

    @Test
    fun `CompetitionSignup handles missing parent entry gracefully`() {
        val source = sourceFile.readText()
        // Find the section around getBackStackEntry specifically
        val getBackStackIdx = source.indexOf("getBackStackEntry")
        assertTrue("getBackStackEntry should exist in source", getBackStackIdx >= 0)
        // The try block wrapping getBackStackEntry should be nearby (within 300 chars before)
        val precedingContext = source.substring(maxOf(0, getBackStackIdx - 300), getBackStackIdx)
        // Count open try blocks - there should be a try that wraps getBackStackEntry directly
        val hasDedicatedTryCatch = precedingContext.contains("try {") &&
            source.substring(getBackStackIdx, minOf(source.length, getBackStackIdx + 300))
                .contains("catch")
        assertTrue(
            "getBackStackEntry() should be wrapped in its own try-catch block for graceful error handling",
            hasDedicatedTryCatch
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue("AppNavHost.kt should exist", sourceFile.exists())
    }

    @Test
    fun `CompetitionSignup composable exists`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should have CompetitionSignup composable",
            source.contains("Screen.CompetitionSignup.route")
        )
    }

    @Test
    fun `competitionId argument extraction uses safe NavigationArguments`() {
        val source = sourceFile.readText()
        assertTrue(
            "competitionId should be extracted via NavigationArguments.requireLong",
            source.contains("NavigationArguments.requireLong(backStackEntry.arguments, \"competitionId\")")
        )
    }

    @Test
    fun `CompetitionSignup has error handling for missing competitionId`() {
        val source = sourceFile.readText()
        val signupSection = source.substringAfter("Screen.CompetitionSignup.route")
        assertTrue(
            "Should have try-catch for competitionId extraction",
            signupSection.contains("catch (e: IllegalArgumentException)")
        )
    }

    @Test
    fun `safePopBackStack is used for error recovery`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should use safePopBackStack for safe navigation on error",
            source.contains("safePopBackStack()")
        )
    }
}
