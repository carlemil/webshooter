package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source-level invariants for the (combined-chart) ResultsTrendsScreen.
 * After the chart-library migration the screen is pure Compose Canvas — these
 * assertions verify the structural invariants that survived without dragging
 * any MPAndroidChart-specific shape into the test surface.
 */
class ChartsScreenTest {

    private val sourceFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsScreen.kt")

    @Test
    fun `ChartsScreen uses shared ChartLegend from ui common`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must import ChartLegend from ui.common",
            source.contains("se.kjellstrand.webshooter.ui.common.ChartLegend")
        )
        assertTrue(
            "ChartsScreen must import UserLegendItem from ui.common",
            source.contains("se.kjellstrand.webshooter.ui.common.UserLegendItem")
        )
        assertTrue("ChartsScreen must call ChartLegend(", source.contains("ChartLegend("))
        assertTrue(
            "ChartsScreen must construct UserLegendItem entries",
            source.contains("UserLegendItem(")
        )
    }

    @Test
    fun `ChartsScreen renders the chart via Compose Canvas (no MPAndroidChart)`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must render via Compose Canvas",
            source.contains("Canvas(")
        )
        assertTrue(
            "ChartsScreen must use the shared drawScatterShape helper",
            source.contains("drawScatterShape(")
        )
        assertFalse(
            "ChartsScreen must NOT use AndroidView (chart is now pure Compose)",
            source.contains("AndroidView")
        )
        assertFalse(
            "ChartsScreen must NOT import MPAndroidChart classes",
            source.contains("com.github.mikephil")
        )
    }

    @Test
    fun `ChartsScreen renders charts_subtitle above the state wrapper`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must reference Res.string.charts_subtitle",
            source.contains("Res.string.charts_subtitle")
        )
        val subtitleIndex = source.indexOf("charts_subtitle")
        val wrapperIndex = source.indexOf("ChartStateWrapper(")
        assertTrue("subtitle must appear before ChartStateWrapper", subtitleIndex in 0 until wrapperIndex)
    }

    @Test
    fun `charts_subtitle string exists with Swedish explanation`() {
        val strings = File("../shared/src/commonMain/composeResources/values/strings.xml").readText()
        assertTrue(
            "strings.xml must define charts_subtitle",
            Regex("""<string\s+name="charts_subtitle">[^<]*</string>""").containsMatchIn(strings)
        )
        assertTrue(
            "charts_subtitle should be 'Snittpoäng per tävling över tid'",
            strings.contains(">Snittpoäng per tävling över tid<")
        )
    }

    @Test
    fun `ChartScatterChart signature accepts myAverage and myTrend`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartScatterChart must accept myAverage: Float? parameter",
            Regex("""myAverage\s*:\s*Float\?""").containsMatchIn(source)
        )
        assertTrue(
            "ChartScatterChart must accept myTrend: ChartsUiState.TrendLine? parameter",
            Regex("""myTrend\s*:\s*ChartsUiState\.TrendLine\?""").containsMatchIn(source)
        )
    }

    @Test
    fun `ChartsScreen draws an average horizontal line when myAverage is set`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must draw the average line via Compose drawLine",
            source.contains("if (myAverage != null)") &&
                Regex("""drawLine\s*\(""").containsMatchIn(source)
        )
    }

    @Test
    fun `ChartsScreen renders trend as a 2-point line using myTrend's fromY and toY`() {
        val source = sourceFile.readText()
        assertTrue(
            "Trend rendering must use myTrend's fromY and toY as line endpoints",
            source.contains("myTrend.fromY") && source.contains("myTrend.toY")
        )
    }

    @Test
    fun `ChartsContent passes myAverage and myTrend to the chart`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsContent must pass uiState.myAverage to ChartScatterChart",
            source.contains("myAverage = uiState.myAverage")
        )
        assertTrue(
            "ChartsContent must pass uiState.myTrend to ChartScatterChart",
            source.contains("myTrend = uiState.myTrend")
        )
    }

    @Test
    fun `ChartsScreen marker tooltip formats date and points with 'p' unit`() {
        val source = sourceFile.readText()
        assertTrue(
            "Marker tooltip must format the underlying averageSerieScore as '<value> p'",
            source.contains("averageSerieScore") &&
                Regex("""\"[^\"]* p\"""").containsMatchIn(source)
        )
    }

    @Test
    fun `ResultsTrendsScreenKt composable file exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsScreenKt")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ResultsTrendsScreenKt should exist", clazz)
        val methods = clazz!!.declaredMethods.map { it.name }
        assertTrue("Should have ChartsScreen composable", methods.any { it == "ChartsScreen" })
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ResultsTrendsViewModel interface exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModel")
        assertTrue(clazz.isInterface)
    }

    @Test
    fun `ChartsUiState class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ChartsUiState")
        assertNotNull(clazz)
    }

    @Test
    fun `WeaponClassBadge composable exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.common.WeaponClassBadgesKt")
        assertNotNull(clazz)
    }
}
