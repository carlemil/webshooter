package se.kjellstrand.webshooter.ui.screens.myresults

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class MyResultsScreenTest {

    private val sourceFile = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/myresults/MyResultsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `loading stats spinner does not use small 24dp size`() {
        val source = sourceFile.readText()
        val loadingStatsSection = source.substringAfter("isLoadingStats")
            .substringBefore("header_all_time")
        assertFalse(
            "Loading stats spinner should not use small 24.dp size",
            loadingStatsSection.contains("size(24.dp)")
        )
    }

    @Test
    fun `loading stats section has explanatory text`() {
        val source = sourceFile.readText()
        val loadingStatsSection = source.substringAfter("isLoadingStats")
            .substringBefore("header_all_time")
        assertTrue(
            "Loading stats section should include explanatory text",
            loadingStatsSection.contains("Text(") || loadingStatsSection.contains("Res.string.loading")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue(sourceFile.exists())
    }

    @Test
    fun `has loading stats check`() {
        val source = sourceFile.readText()
        assertTrue(source.contains("isLoadingStats"))
    }

    @Test
    fun `has CircularProgressIndicator for loading stats`() {
        val source = sourceFile.readText()
        val loadingStatsSection = source.substringAfter("isLoadingStats")
            .substringBefore("header_all_time")
        assertTrue(loadingStatsSection.contains("CircularProgressIndicator"))
    }
}
