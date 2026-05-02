package se.kjellstrand.webshooter.ui.screens.seriespoints

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SeriesPointsScreenTest {

    private val sourceFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/seriespoints/SeriesPointsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `SeriesPointsScreen defines SeriesPointsMarkerView subclass`() {
        val source = sourceFile.readText()
        assertTrue(
            "SeriesPointsScreen must define a SeriesPointsMarkerView class extending MarkerView",
            source.contains("class SeriesPointsMarkerView") &&
                source.contains("MarkerView(context, R.layout.marker_view)")
        )
        assertTrue(
            "SeriesPointsScreen must import MarkerView",
            source.contains("com.github.mikephil.charting.components.MarkerView")
        )
    }

    @Test
    fun `SeriesPointsScreen attaches SeriesPointsMarkerView to the chart`() {
        val source = sourceFile.readText()
        assertTrue(
            "chart.marker must be assigned to a SeriesPointsMarkerView",
            source.contains("chart.marker = SeriesPointsMarkerView(")
        )
    }

    @Test
    fun `SeriesPointsMarkerView labels include points and 'p' unit`() {
        val source = sourceFile.readText()
        assertTrue(
            "Label template must include a 'p' unit for points in the tooltip",
            source.contains(" p\"")
        )
    }

    @Test
    fun `SeriesPointsMarkerView uses a keyed map for dataSetIndex and xIndex`() {
        val source = sourceFile.readText()
        assertTrue(
            "Marker labels must be stored in a Map<Pair<Int, Int>, String> or equivalent",
            source.contains("Map<Pair<Int, Int>, String>") ||
                source.contains("mutableMapOf<Pair<Int, Int>, String>()") ||
                source.contains("mapOf<Pair<Int, Int>, String>")
        )
        assertTrue(
            "Marker refreshContent must read highlight.dataSetIndex",
            source.contains("dataSetIndex")
        )
    }

    @Test
    fun `SeriesPointsScreen renders series_points_subtitle above the state wrapper`() {
        val source = sourceFile.readText()
        assertTrue(
            "SeriesPointsScreen must reference R.string.series_points_subtitle",
            source.contains("R.string.series_points_subtitle")
        )
        val subtitleIdx = source.indexOf("series_points_subtitle")
        val wrapperIdx = source.indexOf("ChartStateWrapper(")
        assertTrue(
            "subtitle must appear before ChartStateWrapper",
            subtitleIdx in 0 until wrapperIdx
        )
    }

    @Test
    fun `series_points_subtitle string exists with Swedish explanation`() {
        val strings =
            File("src/main/res/values/strings.xml").readText()
        assertTrue(
            "strings.xml must define series_points_subtitle",
            Regex("""<string\s+name="series_points_subtitle">[^<]*</string>""").containsMatchIn(
                strings
            )
        )
        assertTrue(
            "series_points_subtitle should be 'Poäng per serie i precisions tävlingar'",
            strings.contains(">Poäng per serie i precisions tävlingar<")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `SeriesPointsScreen source file exists`() {
        assertTrue(sourceFile.exists())
    }

    @Test
    fun `SeriesPointsScreen still uses LineChart via AndroidView`() {
        val source = sourceFile.readText()
        assertTrue(source.contains("LineChart"))
        assertTrue(source.contains("AndroidView"))
    }

    @Test
    fun `SeriesPointsScreen preserves reversed ordering for newest on top`() {
        val source = sourceFile.readText()
        assertTrue(
            "SeriesPointsScreen must still reverse dataSets so newest renders on top",
            source.contains("dataSets.reverse()") ||
                source.contains("reversed()")
        )
    }
}
