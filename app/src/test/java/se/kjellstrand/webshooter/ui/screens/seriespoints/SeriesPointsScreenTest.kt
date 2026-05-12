package se.kjellstrand.webshooter.ui.screens.seriespoints

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SeriesPointsScreenTest {

    private val sourceFile =
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/seriespoints/SeriesPointsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `SeriesPointsScreen marker tooltip uses points + 'p' unit`() {
        val source = sourceFile.readText()
        // The marker overlay is now a Compose Box with a Text composable —
        // confirm the points + "p" formatting survived the migration.
        assertTrue(
            "Marker tooltip must format the value as '<points> p'",
            source.contains(" p\"")
        )
    }

    @Test
    fun `SeriesPointsScreen renders series_points_subtitle above the state wrapper`() {
        val source = sourceFile.readText()
        assertTrue(
            "SeriesPointsScreen must reference Res.string.series_points_subtitle",
            source.contains("Res.string.series_points_subtitle")
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
            File("../shared/src/commonMain/composeResources/values/strings.xml").readText()
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
    fun `SeriesPointsScreen renders the chart via Compose Canvas (no MPAndroidChart)`() {
        val source = sourceFile.readText()
        assertTrue(
            "SeriesPointsScreen must render via Compose Canvas",
            source.contains("Canvas(")
        )
        assertFalse(
            "SeriesPointsScreen must NOT use AndroidView (chart is now pure Compose)",
            source.contains("AndroidView")
        )
        assertFalse(
            "SeriesPointsScreen must NOT import MPAndroidChart classes",
            source.contains("com.github.mikephil")
        )
    }

    @Test
    fun `SeriesPointsScreen preserves reversed ordering for newest on top`() {
        val source = sourceFile.readText()
        assertTrue(
            "SeriesPointsScreen must reverse the series so newest renders on top",
            source.contains("reversed()") || source.contains(".reverse()")
        )
    }
}
