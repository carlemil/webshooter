package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ChartsRelevantUserIdsTest {

    private val uiStateFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsUiState.kt")
    private val screenFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsScreen.kt")
    private val seriesPointsScreen =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/seriespoints/SeriesPointsScreen.kt")

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
    fun `SeriesPointsScreen passes relevantUserIds derived from precisionClubMembers`() {
        val source = seriesPointsScreen.readText()
        assertTrue(
            "SeriesPointsScreen must pass relevantUserIds derived from precisionClubMembers",
            source.contains("relevantUserIds") &&
                source.contains("precisionClubMembers") &&
                (source.contains(".map { it.userId }.toSet()") ||
                    source.contains("precisionClubMembers.map { it.userId }"))
        )
        assertTrue(
            "SeriesPointsScreen should pass full clubMembers list now that filtering is handled by the dialog",
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
            "src/main/java/se/kjellstrand/webshooter/ui/screens/seriespoints/SeriesPointsUiState.kt"
        ).readText()
        assertTrue(source.contains("precisionClubMembers"))
    }
}
