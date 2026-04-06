package se.kjellstrand.webshooter.ui.screens.competitions

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CompetitionsScreenTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/ui/screens/competitions/CompetitionsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `items call for filteredData uses key parameter`() {
        val source = sourceFile.readText()
        // The items(filteredData) call should have a key parameter
        val hasKeylessItems = Regex("""items\(filteredData\)\s*\{""").containsMatchIn(source)
        assertFalse(
            "items(filteredData) should use key parameter, e.g. items(filteredData, key = { it.id })",
            hasKeylessItems
        )
    }

    @Test
    fun `items call for filteredData uses id as key`() {
        val source = sourceFile.readText()
        val hasKeyedItems = source.contains("items(filteredData, key = { it.id })")
        assertTrue(
            "items(filteredData) should use key = { it.id }",
            hasKeyedItems
        )
    }

    @Test
    fun `CompetitionItem is decomposed with CompetitionItemHeader`() {
        val source = sourceFile.readText()
        assertTrue(
            "CompetitionItem should call extracted CompetitionItemHeader composable",
            source.contains("fun CompetitionItemHeader(")
        )
    }

    @Test
    fun `CompetitionItem is decomposed with CompetitionItemButtons`() {
        val source = sourceFile.readText()
        assertTrue(
            "CompetitionItem should call extracted CompetitionItemButtons composable",
            source.contains("fun CompetitionItemButtons(")
        )
    }

    // --- Fixed behavior for previews (should FAIL before fix, PASS after fix) ---

    @Test
    fun `CompetitionsScreen should have loading preview`() {
        val source = sourceFile.readText()
        assertTrue("Should have CompetitionsScreenLoadingPreview", source.contains("CompetitionsScreenLoadingPreview"))
    }

    @Test
    fun `CompetitionsScreen should have error preview`() {
        val source = sourceFile.readText()
        assertTrue("Should have CompetitionsScreenErrorPreview", source.contains("CompetitionsScreenErrorPreview"))
    }

    @Test
    fun `CompetitionsScreen should have empty preview`() {
        val source = sourceFile.readText()
        assertTrue("Should have CompetitionsScreenEmptyPreview", source.contains("CompetitionsScreenEmptyPreview"))
    }

    @Test
    fun `CompetitionsViewModelMock should accept UiState constructor parameter`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock")
        val hasUiStateParam = clazz.declaredConstructors.any { c ->
            c.parameterTypes.any { it.simpleName == "CompetitionsUiState" }
        }
        assertTrue("CompetitionsViewModelMock should accept CompetitionsUiState param", hasUiStateParam)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue("CompetitionsScreen.kt should exist", sourceFile.exists())
    }

    @Test
    fun `source file contains items call with filteredData`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should have an items() call referencing filteredData",
            source.contains("items(filteredData")
        )
    }

    @Test
    fun `source file contains CompetitionItem composable`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should contain CompetitionItem composable call",
            source.contains("CompetitionItem(")
        )
    }
}
