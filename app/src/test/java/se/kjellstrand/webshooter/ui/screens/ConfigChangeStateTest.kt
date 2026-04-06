package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ConfigChangeStateTest {

    private val signupsScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/signups/SignupsScreen.kt")
    private val resultsScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/results/ResultsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `SignupsScreen isFilterSheetOpen uses rememberSaveable`() {
        val source = signupsScreen.readText()
        assertTrue(
            "isFilterSheetOpen should use rememberSaveable to survive config changes",
            source.contains("var isFilterSheetOpen by rememberSaveable")
        )
    }

    @Test
    fun `ResultsScreen isFilterBottomSheetOpen uses rememberSaveable`() {
        val source = resultsScreen.readText()
        assertTrue(
            "isFilterBottomSheetOpen should use rememberSaveable to survive config changes",
            source.contains("var isFilterBottomSheetOpen by rememberSaveable")
        )
    }

    @Test
    fun `ResultsScreen isRefreshing uses rememberSaveable`() {
        val source = resultsScreen.readText()
        assertTrue(
            "isRefreshing should use rememberSaveable to survive config changes",
            source.contains("var isRefreshing by rememberSaveable")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `SignupsScreen exists`() {
        assertTrue(signupsScreen.exists())
    }

    @Test
    fun `ResultsScreen exists`() {
        assertTrue(resultsScreen.exists())
    }

    @Test
    fun `SignupsScreen has filter sheet state`() {
        val source = signupsScreen.readText()
        assertTrue(source.contains("isFilterSheetOpen"))
    }

    @Test
    fun `ResultsScreen has filter bottom sheet state`() {
        val source = resultsScreen.readText()
        assertTrue(source.contains("isFilterBottomSheetOpen"))
    }
}
