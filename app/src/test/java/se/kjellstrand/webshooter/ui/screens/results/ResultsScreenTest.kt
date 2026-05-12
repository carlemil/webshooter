package se.kjellstrand.webshooter.ui.screens.results

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ResultsScreenTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/ui/screens/results/ResultsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ResultsList is decomposed with GroupHeaderItem`() {
        val source = sourceFile.readText()
        assertTrue(
            "ResultsList should call extracted GroupHeaderItem composable",
            source.contains("fun GroupHeaderItem(")
        )
    }

    @Test
    fun `ResultsList is decomposed with GroupedResultItem`() {
        val source = sourceFile.readText()
        assertTrue(
            "ResultsList should call extracted GroupedResultItem composable",
            source.contains("fun GroupedResultItem(")
        )
    }

    @Test
    fun `GroupingAndFilterBottomSheet does not contain hardcoded Gruppering`() {
        val source = sourceFile.readText()
        assertFalse(
            "Should use stringResource instead of hardcoded 'Gruppering'",
            source.contains("Text(\"Gruppering\"")
        )
    }

    @Test
    fun `GroupingMode labels do not contain hardcoded Swedish strings`() {
        val source = sourceFile.readText()
        val bottomSheetSection = source.substringAfter("GroupingAndFilterBottomSheet")
        assertFalse(
            "Should use stringResource instead of hardcoded 'Vapenklass'",
            bottomSheetSection.contains("\"Vapenklass\"")
        )
        assertFalse(
            "Should use stringResource instead of hardcoded 'Klubb'",
            bottomSheetSection.contains("\"Klubb\"")
        )
        assertFalse(
            "Should use stringResource instead of hardcoded 'Medl'",
            bottomSheetSection.contains("\"Medl\"")
        )
        assertFalse(
            "Should use stringResource instead of hardcoded 'Ingen'",
            bottomSheetSection.contains("\"Ingen\"")
        )
    }

    @Test
    fun `GroupingMode labels use stringResource`() {
        val source = sourceFile.readText()
        assertTrue(
            "GroupingMode labels should use stringResource for localization",
            source.contains("Res.string.results_grouping")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue("ResultsScreen.kt should exist", sourceFile.exists())
    }

    @Test
    fun `source file contains ResultItem composable`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should contain ResultItem composable",
            source.contains("ResultItem(")
        )
    }

    @Test
    fun `source file contains ResultsListHeader`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should contain ResultsListHeader composable",
            source.contains("ResultsListHeader(")
        )
    }
}
