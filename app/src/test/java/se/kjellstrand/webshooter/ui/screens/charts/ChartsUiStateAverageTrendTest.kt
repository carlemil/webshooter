package se.kjellstrand.webshooter.ui.screens.charts

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ChartsUiStateAverageTrendTest {

    private val uiStateFile =
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsUiState.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartsUiState exposes myAverage Float property`() {
        val source = uiStateFile.readText()
        assertTrue(
            "ChartsUiState must expose val myAverage: Float?",
            Regex("""val\s+myAverage\s*:\s*Float\?""").containsMatchIn(source)
        )
    }

    @Test
    fun `ChartsUiState declares TrendLine data class`() {
        val source = uiStateFile.readText()
        assertTrue(
            "ChartsUiState must declare a TrendLine data class with fromX, fromY, toX, toY Floats",
            source.contains("data class TrendLine") &&
                source.contains("fromX") && source.contains("fromY") &&
                source.contains("toX") && source.contains("toY")
        )
    }

    @Test
    fun `ChartsUiState exposes myTrend property`() {
        val source = uiStateFile.readText()
        assertTrue(
            "ChartsUiState must expose val myTrend: TrendLine?",
            Regex("""val\s+myTrend\s*:\s*(ChartsUiState\.)?TrendLine\?""").containsMatchIn(source)
        )
    }

    @Test
    fun `myTrend uses least-squares regression math`() {
        val source = uiStateFile.readText()
        // Regression requires meanX/meanY/slope-intercept math — look for recognizable fragments
        assertTrue(
            "myTrend should compute slope from sum of (x-meanX)*(y-meanY)",
            source.contains("meanX") && source.contains("meanY") &&
                source.contains("slope") && source.contains("intercept")
        )
    }

    @Test
    fun `both properties derive from filteredChartData`() {
        val source = uiStateFile.readText()
        // Both should reference filteredChartData so they react to filters
        val myAverageBlock = Regex("""myAverage[^\n]*[\s\S]{0,400}""").findAll(source).joinToString("\n") { it.value }
        val myTrendBlock = Regex("""myTrend[^\n]*[\s\S]{0,800}""").findAll(source).joinToString("\n") { it.value }
        assertTrue(
            "myAverage must derive from filteredChartData",
            myAverageBlock.contains("filteredChartData")
        )
        assertTrue(
            "myTrend must derive from filteredChartData",
            myTrendBlock.contains("filteredChartData")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsUiState still exists`() {
        assertTrue(uiStateFile.exists())
    }

    @Test
    fun `ChartsUiState still exposes filteredChartData and relevantUserIds`() {
        val source = uiStateFile.readText()
        assertTrue(source.contains("filteredChartData"))
        assertTrue(source.contains("relevantUserIds"))
    }
}
