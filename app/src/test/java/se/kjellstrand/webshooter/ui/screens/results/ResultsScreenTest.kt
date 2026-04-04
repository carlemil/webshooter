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
