package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ChartsRelevantUserIdsTest {

    private val uiStateFile =
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsUiState.kt")
    private val screenFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsScreen.kt")
    private val seriesPointsScreen =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/seriespoints/SeriesPointsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartsUiState exposes relevantUserIds set`() {
        val source = uiStateFile.readText()
        assertTrue(
            "ChartsUiState must expose a relevantUserIds: Set<Long> computed property",
            Regex("""val\s+relevantUserIds\s*:\s*Set<Long>""").containsMatchIn(source)
        )
    }

    @Test
    fun `ChartsScreen passes relevantUserIds to ShooterPickerDialog`() {
        val source = screenFile.readText()
        assertTrue(
            "ChartsScreen must pass relevantUserIds = uiState.relevantUserIds to the dialog",
            source.contains("relevantUserIds = uiState.relevantUserIds") ||
                Regex("""relevantUserIds\s*=\s*uiState\.relevantUserIds""").containsMatchIn(source)
        )
    }

    @Test
    fun `SeriesPointsScreen passes full clubMembers and unfiltered relevantUserIds to dialog`() {
        val source = seriesPointsScreen.readText()
        assertTrue(
            "SeriesPointsScreen should still reference relevantUserIds parameter on the dialog",
            source.contains("relevantUserIds")
        )
        assertTrue(
            "SeriesPointsScreen should pass full clubMembers list (search-all-clubs design)",
            source.contains("clubMembers = uiState.clubMembers")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsUiState still exists`() {
        assertTrue(uiStateFile.exists())
    }

    @Test
    fun `ChartsUiState still exposes filteredChartData and filteredComparedShooters`() {
        val source = uiStateFile.readText()
        assertTrue(source.contains("filteredChartData"))
        assertTrue(source.contains("filteredComparedShooters"))
    }

    @Test
    fun `SeriesPointsUiState still exposes precisionClubMembers`() {
        val source = File(
            "../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/seriespoints/SeriesPointsUiState.kt"
        ).readText()
        assertTrue(source.contains("precisionClubMembers"))
    }
}
