package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Verifies wiring of the M.-prec tab: grouping by trends tab key, conditional
 * inclusion in availableResultsTypes, the trendsTabDisplayName mapping, and the
 * compared-shooter filter using the same tab-key bucketing. ViewModel/Screen
 * shape is checked via source-regex tests because the existing tests in this
 * package use that same pattern.
 */
class MagnumprecisionTabTest {

    private val vmImplFile =
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsViewModelImpl.kt")
    private val screenFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsScreen.kt")
    private val uiStateFile =
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsUiState.kt")
    private val tabKeyFile =
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/TrendsTabKey.kt")

    private fun callTrendsTabDisplayName(tabKey: String): String? {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.TrendsTabKeyKt")
        } catch (_: ClassNotFoundException) {
            return null
        }
        val method = clazz.declaredMethods.find { it.name == "trendsTabDisplayName" } ?: return null
        return method.invoke(null, tabKey) as? String
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `loadChartData groups dataPoints by trendsTabKeyFor`() {
        val source = vmImplFile.readText()
        assertTrue(
            "loadChartData must groupBy trendsTabKeyFor(competitionTypeName, resultsType)",
            Regex(
                """groupBy\s*\{[\s\S]{0,120}trendsTabKeyFor\s*\(\s*it\.competitionTypeName\s*,\s*it\.resultsType\s*\)"""
            ).containsMatchIn(source)
        )
        assertFalse(
            "loadChartData must no longer groupBy raw resultsType",
            Regex("""groupBy\s*\{\s*it\.resultsType\s*\}""").containsMatchIn(source)
        )
    }

    @Test
    fun `loadChartData appends magnumprecision to availableTypes only when bucket has data`() {
        val source = vmImplFile.readText()
        // System tabs derived using trendsTabKeyFor on metadata
        assertTrue(
            "system tabs must be derived from allCompetitionMeta via trendsTabKeyFor",
            Regex(
                """allCompetitionMeta[\s\S]{0,300}trendsTabKeyFor\s*\([\s\S]{0,80}competitionTypeName[\s\S]{0,80}resultsType"""
            ).containsMatchIn(source)
        )
        // Conditional append using grouped.containsKey(MAGNUMPRECISION_TAB_KEY)
        assertTrue(
            "magnumprecision tab key must be appended only when grouped contains it",
            Regex(
                """grouped\s*\.\s*containsKey\s*\(\s*MAGNUMPRECISION_TAB_KEY\s*\)"""
            ).containsMatchIn(source)
        )
    }

    @Test
    fun `trendsTabDisplayName maps magnumprecision to M dot dash prec`() {
        assertEquals("M.-prec", callTrendsTabDisplayName(MAGNUMPRECISION_TAB_KEY))
    }

    @Test
    fun `trendsTabDisplayName returns precision Swedish name from ResultsType`() {
        assertEquals("Precision", callTrendsTabDisplayName("precision"))
        assertEquals("Fält", callTrendsTabDisplayName("field"))
        assertEquals("Militär", callTrendsTabDisplayName("military"))
    }

    @Test
    fun `trendsTabDisplayName falls back to capitalized key for unknown tabs`() {
        assertEquals("Other", callTrendsTabDisplayName("other"))
    }

    @Test
    fun `trendsTabDisplayName lives in TrendsTabKey file`() {
        val source = tabKeyFile.readText()
        assertTrue(
            "trendsTabDisplayName must live in TrendsTabKey.kt next to trendsTabKeyFor",
            Regex("""fun\s+trendsTabDisplayName\s*\(\s*tabKey\s*:\s*String\s*\)\s*:\s*String""")
                .containsMatchIn(source)
        )
    }

    @Test
    fun `Screen Tab text uses trendsTabDisplayName helper`() {
        val source = screenFile.readText()
        assertTrue(
            "Tab text must call trendsTabDisplayName(...)",
            Regex("""trendsTabDisplayName\s*\(""").containsMatchIn(source)
        )
        assertFalse(
            "Tab text must no longer use the inline ResultsType.fromApiString fallback expression",
            Regex(
                """ResultsType\.fromApiString\s*\([^)]*\)\s*\?\.\s*displayName\s*\n?\s*\?:\s*\w+\.replaceFirstChar"""
            ).containsMatchIn(source)
        )
    }

    @Test
    fun `filteredComparedShooters uses trendsTabKeyFor for tab-key matching`() {
        val source = uiStateFile.readText()
        // Locate the filteredComparedShooters block to scope the regex.
        val block = Regex(
            """filteredComparedShooters[\s\S]*?(?=\n\s*val\s+\w|\n\}\s*$)"""
        ).find(source)?.value
            ?: error("Could not find filteredComparedShooters block")
        assertTrue(
            "filteredComparedShooters must compare via trendsTabKeyFor(...)",
            Regex("""trendsTabKeyFor\s*\([^)]*selectedResultsType""").containsMatchIn(block) ||
                Regex("""trendsTabKeyFor\s*\([\s\S]{0,80}\)\s*==\s*selectedResultsType""")
                    .containsMatchIn(block)
        )
        assertFalse(
            "filteredComparedShooters must no longer compare raw it.resultsType to selectedResultsType",
            Regex("""it\.resultsType\s*==\s*selectedResultsType""").containsMatchIn(block)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `MAGNUMPRECISION_TAB_KEY constant is the magnumprecision string`() {
        assertEquals("magnumprecision", MAGNUMPRECISION_TAB_KEY)
    }

    @Test
    fun `pointfield tab is still hidden`() {
        val source = vmImplFile.readText()
        assertTrue(
            "pointfield must still be in the hidden set so it is filtered from the tab row",
            Regex("""hiddenTypes\s*=\s*setOf\s*\(\s*\"pointfield\"\s*\)""").containsMatchIn(source)
        )
    }

    @Test
    fun `selectTab still updates selectedResultsType`() {
        val source = vmImplFile.readText()
        val block = Regex(
            """fun\s+selectTab\s*\([^)]*\)\s*\{[\s\S]*?\n\s*\}"""
        ).find(source)?.value
            ?: error("Could not find selectTab")
        assertTrue(block.contains("selectedResultsType"))
    }

    @Test
    fun `filteredChartData still filters by averageSerieScore greater than zero`() {
        val source = uiStateFile.readText()
        assertTrue(
            "filteredChartData must keep its zero-score filter",
            Regex("""it\.averageSerieScore\s*>\s*0\.0""").containsMatchIn(source)
        )
    }
}
