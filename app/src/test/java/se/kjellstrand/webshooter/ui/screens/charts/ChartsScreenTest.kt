package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.lang.reflect.Modifier

class ChartsScreenTest {

    private val sourceFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartsScreen uses shared UserLegend from ui common`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must import UserLegend from ui.common",
            source.contains("se.kjellstrand.webshooter.ui.common.UserLegend")
        )
        assertTrue(
            "ChartsScreen must import UserLegendItem from ui.common",
            source.contains("se.kjellstrand.webshooter.ui.common.UserLegendItem")
        )
        assertTrue("ChartsScreen must call UserLegend(", source.contains("UserLegend("))
        assertTrue(
            "ChartsScreen must construct UserLegendItem entries",
            source.contains("UserLegendItem(")
        )
    }

    @Test
    fun `ChartsScreen disables MPAndroidChart built-in legend`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must set legend.isEnabled = false so the composable UserLegend is the sole legend",
            source.contains("legend.isEnabled = false")
        )
        assertFalse(
            "Old built-in legend styling must be removed (legend.isWordWrapEnabled)",
            source.contains("legend.isWordWrapEnabled")
        )
    }

    @Test
    fun `ChartsScreen uses shapeRenderer from CHART_SHAPE_RENDERERS`() {
        val source = sourceFile.readText()
        assertTrue(
            "Scatter datasets must use shapeRenderer = CHART_SHAPE_RENDERERS[...] so shapes match the composable legend",
            source.contains("shapeRenderer = CHART_SHAPE_RENDERERS")
        )
        assertFalse(
            "Old setScatterShape(CHART_SHAPES[...]) calls must be replaced with shapeRenderer",
            source.contains("setScatterShape(CHART_SHAPES")
        )
    }

    @Test
    fun `ChartsScreen defines ChartsMarkerView subclass`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must define a ChartsMarkerView class extending MarkerView",
            source.contains("class ChartsMarkerView") &&
                source.contains("MarkerView(context, R.layout.marker_view)")
        )
        assertTrue(
            "ChartsScreen must import MarkerView",
            source.contains("com.github.mikephil.charting.components.MarkerView")
        )
    }

    @Test
    fun `ChartsScreen attaches ChartsMarkerView to the chart`() {
        val source = sourceFile.readText()
        assertTrue(
            "chart.marker must be assigned to a ChartsMarkerView",
            source.contains("chart.marker = ChartsMarkerView(")
        )
    }

    @Test
    fun `ChartsScreen renders charts_subtitle above the state wrapper`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must reference R.string.charts_subtitle",
            source.contains("R.string.charts_subtitle")
        )
        val subtitleIndex = source.indexOf("charts_subtitle")
        val wrapperIndex = source.indexOf("ChartStateWrapper(")
        assertTrue("subtitle must appear before ChartStateWrapper", subtitleIndex in 0 until wrapperIndex)
    }

    @Test
    fun `charts_subtitle string exists with Swedish explanation`() {
        val strings = File("src/main/res/values/strings.xml").readText()
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
    fun `ChartsScreen draws average as a LimitLine on axisLeft`() {
        val source = sourceFile.readText()
        assertTrue(
            "ChartsScreen must use LimitLine for the average",
            source.contains("LimitLine") &&
                (source.contains("axisLeft.removeAllLimitLines") ||
                    source.contains("axisLeft.addLimitLine"))
        )
        assertTrue(
            "ChartsScreen must import LimitLine",
            source.contains("com.github.mikephil.charting.components.LimitLine")
        )
    }

    @Test
    fun `ChartsScreen renders trend as a sampled scatter dataset`() {
        val source = sourceFile.readText()
        assertTrue(
            "Trend rendering must sample points along the regression line",
            source.contains("myTrend") &&
                (Regex("""TREND_SAMPLES""").containsMatchIn(source) ||
                    Regex("""0\.\.\s*\d{2,}""").containsMatchIn(source) ||
                    source.contains("trendSamples") ||
                    source.contains("nSamples"))
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
    fun `ChartsScreen builds label map keyed by Entry data tag`() {
        val source = sourceFile.readText()
        assertTrue(
            "Entries must carry a tag via Entry.data for lookup in the marker",
            source.contains(".data =") || source.contains("data = ")
        )
        assertTrue(
            "Marker label should include the date and averageSerieScore unit",
            source.contains("averageSerieScore") &&
                Regex("""\"[^\"]* p\"""").containsMatchIn(source)
        )
    }

    @Test
    fun `ChartsScreen composable function exists`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsScreenKt")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("ChartsScreenKt should exist", clazz)
        val methods = clazz!!.declaredMethods.map { it.name }
        assertTrue("Should have ChartsScreen composable", methods.any { it == "ChartsScreen" })
    }

    @Test
    fun `ChartLineChart composable function exists for chart rendering`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsScreenKt")
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("Should have ChartLineChart composable", methods.any { it == "ChartLineChart" })
    }

    @Test
    fun `AddShooterDialog composable function exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsScreenKt")
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("Should have AddShooterDialog composable", methods.any { it == "AddShooterDialog" })
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsViewModel interface exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModel")
        assertTrue(clazz.isInterface)
    }

    @Test
    fun `ChartsUiState class exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.charts.ChartsUiState")
        assertNotNull(clazz)
    }

    @Test
    fun `WeaponClassBadge composable exists`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.common.WeaponClassBadgesKt")
        assertNotNull(clazz)
    }

    @Test
    fun `MPAndroidChart LineChart class is available`() {
        val clazz = Class.forName("com.github.mikephil.charting.charts.LineChart")
        assertNotNull(clazz)
    }
}
