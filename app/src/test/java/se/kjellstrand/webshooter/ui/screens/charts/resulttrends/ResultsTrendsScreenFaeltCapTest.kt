package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the field/pointfield "hits-based" axis behavior on the combined
 * trends chart. Pre-migration these were anchored to MPAndroidChart's
 * `axisLeft.axisMaximum`; post-migration the equivalent is the `yMax`
 * computation inside [ChartScatterChart] (`if (isHitsBased) 6f else …`)
 * plus the `!isHitsBased` guards around the trendline and its legend entry.
 */
class ResultsTrendsScreenFaeltCapTest {

    private val screenSource: String by lazy {
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsScreen.kt").readText()
    }

    @Test
    fun `ChartsContent derives isHitsBased from selectedResultsType`() {
        assertTrue(
            "ChartsContent should derive an isHitsBased flag from selectedResultsType == 'field' or 'pointfield'",
            screenSource.contains("isHitsBased") &&
                screenSource.contains("\"field\"") &&
                screenSource.contains("\"pointfield\"")
        )
    }

    @Test
    fun `ChartScatterChart exposes an isHitsBased parameter`() {
        val sigStart = screenSource.indexOf("fun ChartScatterChart(")
        assertTrue("ChartScatterChart must still exist", sigStart >= 0)
        val sigEnd = screenSource.indexOf(')', sigStart)
        assertTrue("ChartScatterChart signature should be closeable", sigEnd > sigStart)
        val signature = screenSource.substring(sigStart, sigEnd + 1)
        assertTrue(
            "ChartScatterChart should declare an isHitsBased: Boolean parameter, saw: $signature",
            Regex("isHitsBased\\s*:\\s*Boolean").containsMatchIn(signature)
        )
    }

    @Test
    fun `yMax is capped at 6 when isHitsBased`() {
        // The Compose-Canvas equivalent of the old `axisLeft.axisMaximum = 6f`
        // is the yMax expression: `if (isHitsBased) 6f else …`.
        assertTrue(
            "yMax should resolve to 6f when isHitsBased is true",
            Regex("""yMax\s*=\s*if\s*\(\s*isHitsBased\s*\)\s*6f""").containsMatchIn(screenSource) ||
                Regex("""if\s*\(\s*isHitsBased\s*\)\s*6f""").containsMatchIn(screenSource)
        )
    }

    @Test
    fun `ChartScatterChart suppresses the trendline when isHitsBased`() {
        // The trendline drawing must be guarded by !isHitsBased.
        assertTrue(
            "Trendline rendering must be guarded by !isHitsBased",
            screenSource.contains("!isHitsBased")
        )
    }

    @Test
    fun `ChartsContent suppresses the trend legend entry when hits-based`() {
        val idx = screenSource.indexOf("charts_legend_trend")
        assertTrue("charts_legend_trend legend entry should still be referenced", idx >= 0)
        // Look at ~300 chars of context before the usage to find the guard.
        val window = screenSource.substring(
            (idx - 300).coerceAtLeast(0),
            (idx + 100).coerceAtMost(screenSource.length)
        )
        assertTrue(
            "Trend legend entry must be guarded by !isHitsBased (no trendline → no legend row)",
            window.contains("!isHitsBased")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `precision branch still allows auto-scaled axis (no unconditional yMax)`() {
        // The fix should not force yMax = 6f unconditionally; it must be inside
        // a branch that depends on isHitsBased.
        val idx = screenSource.indexOf("6f")
        if (idx >= 0) {
            val window = screenSource.substring(
                (idx - 200).coerceAtLeast(0),
                (idx + 100).coerceAtMost(screenSource.length)
            )
            assertTrue(
                "yMax=6f must sit inside an isHitsBased branch, not run unconditionally",
                window.contains("isHitsBased")
            )
        }
    }

    @Test
    fun `ChartScatterChart still has myAverage and myTrend parameters`() {
        val sigStart = screenSource.indexOf("fun ChartScatterChart(")
        assertTrue("ChartScatterChart must still exist", sigStart >= 0)
        val sigEnd = screenSource.indexOf(')', sigStart)
        val signature = screenSource.substring(sigStart, sigEnd + 1)
        assertTrue(
            "myAverage parameter must remain",
            signature.contains("myAverage")
        )
        assertTrue(
            "myTrend parameter must remain",
            signature.contains("myTrend")
        )
    }

    @Test
    fun `WeaponClassGroupFilter still rendered in ChartsContent`() {
        assertTrue(
            "WeaponClassGroupFilter should still be rendered",
            screenSource.contains("WeaponClassGroupFilter(")
        )
    }
}
