package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ChartsLegendAverageTrendTest {

    private val screenFile =
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsScreen.kt")
    private val stringsFile = File("../shared/src/commonMain/composeResources/values/strings.xml")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `charts_legend_average and charts_legend_trend strings exist in Swedish`() {
        val strings = stringsFile.readText()
        assertTrue(
            "strings.xml must define charts_legend_average",
            strings.contains("<string name=\"charts_legend_average\">Snitt</string>")
        )
        assertTrue(
            "strings.xml must define charts_legend_trend",
            strings.contains("<string name=\"charts_legend_trend\">Trend</string>")
        )
    }

    @Test
    fun `ChartsContent appends legend item for average when myAverage not null`() {
        val source = screenFile.readText()
        assertTrue(
            "ChartsContent must reference charts_legend_average when building legend items",
            source.contains("Res.string.charts_legend_average")
        )
        assertTrue(
            "Average legend entry must gate on uiState.myAverage != null",
            Regex("""uiState\.myAverage\s*!=\s*null""").containsMatchIn(source)
        )
    }

    @Test
    fun `ChartsContent appends legend item for trend when myTrend not null`() {
        val source = screenFile.readText()
        assertTrue(
            "ChartsContent must reference charts_legend_trend when building legend items",
            source.contains("Res.string.charts_legend_trend")
        )
        assertTrue(
            "Trend legend entry must gate on uiState.myTrend != null",
            Regex("""uiState\.myTrend\s*!=\s*null""").containsMatchIn(source)
        )
    }

    @Test
    fun `Legend entries for average and trend use shapeIndex 7 and 8`() {
        val source = screenFile.readText()
        assertTrue(
            "Average legend item must use shapeIndex 7 (horizontal line)",
            Regex("""charts_legend_average[\s\S]{0,500}shapeIndex\s*=\s*7""").containsMatchIn(source) ||
                Regex("""shapeIndex\s*=\s*7[\s\S]{0,500}charts_legend_average""").containsMatchIn(source)
        )
        assertTrue(
            "Trend legend item must use shapeIndex 8 (sloped line)",
            Regex("""charts_legend_trend[\s\S]{0,500}shapeIndex\s*=\s*8""").containsMatchIn(source) ||
                Regex("""shapeIndex\s*=\s*8[\s\S]{0,500}charts_legend_trend""").containsMatchIn(source)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartLegend is still used in ChartsScreen`() {
        val source = screenFile.readText()
        assertTrue(source.contains("ChartLegend("))
    }

    @Test
    fun `ChartsScreen still imports UserLegendItem from ui common`() {
        val source = screenFile.readText()
        assertTrue(source.contains("se.kjellstrand.webshooter.ui.common.UserLegendItem"))
    }
}
