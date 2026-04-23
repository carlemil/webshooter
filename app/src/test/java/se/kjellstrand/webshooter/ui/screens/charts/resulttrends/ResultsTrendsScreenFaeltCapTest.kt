package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ResultsTrendsScreenFaeltCapTest {

    private val screenSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsScreen.kt").readText()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

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
        // Grab the signature of the ChartScatterChart composable and verify the new param.
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
    fun `ChartScatterChart caps axisLeft at 6 when isHitsBased`() {
        assertTrue(
            "ChartScatterChart should set chart.axisLeft.axisMaximum = 6f when isHitsBased",
            Regex("axisLeft\\.axisMaximum\\s*=\\s*6f").containsMatchIn(screenSource)
        )
        assertTrue(
            "ChartScatterChart should reset the axis maximum on the non-hits branch",
            screenSource.contains("resetAxisMaximum()")
        )
    }

    @Test
    fun `axis-max configuration must be applied before chart_data assignment`() {
        // calcMinMax() runs inside `chart.data = combined` and locks in the
        // axis range using the current mCustomAxisMax flag. If we set the
        // flag afterwards, the stale 6f cap from a previous fält render
        // persists on the next precision render → data clusters at the bottom.
        val axisMaxIdx = screenSource.indexOf("chart.axisLeft.axisMaximum = 6f")
        val resetIdx = screenSource.indexOf("chart.axisLeft.resetAxisMaximum()")
        val chartDataIdx = screenSource.indexOf("chart.data = combined")
        assertTrue("axisMaximum=6f must still be set", axisMaxIdx >= 0)
        assertTrue("resetAxisMaximum() must still be called", resetIdx >= 0)
        assertTrue("chart.data = combined must still exist", chartDataIdx >= 0)
        assertTrue(
            "axisMaximum=6f must appear BEFORE chart.data = combined (so calcMinMax sees the right mCustomAxisMax)",
            axisMaxIdx < chartDataIdx
        )
        assertTrue(
            "resetAxisMaximum() must appear BEFORE chart.data = combined",
            resetIdx < chartDataIdx
        )
    }

    @Test
    fun `ChartScatterChart suppresses trendline when isHitsBased`() {
        // The trendLineDataSet construction must be gated on !isHitsBased.
        val trendIdx = screenSource.indexOf("trendLineDataSet")
        assertTrue("trendLineDataSet should still be referenced", trendIdx >= 0)
        // Look at the ~400 chars around the declaration to check the guard condition.
        val window = screenSource.substring(
            (trendIdx - 50).coerceAtLeast(0),
            (trendIdx + 400).coerceAtMost(screenSource.length)
        )
        assertTrue(
            "trendLineDataSet construction must be guarded by !isHitsBased",
            window.contains("!isHitsBased")
        )
    }

    @Test
    fun `cache signature includes isHitsBased`() {
        // The signature buildString must mention isHitsBased so the chart rebuilds
        // when the user toggles tabs between hits-based and points-based modes.
        val sigStart = screenSource.indexOf("val signature = buildString {")
        assertTrue("signature buildString block should exist", sigStart >= 0)
        // The buildString block is terminated by `if (chart.tag == signature) {`.
        val cacheCheck = screenSource.indexOf("if (chart.tag == signature)", sigStart)
        assertTrue("cache check should follow the buildString block", cacheCheck > sigStart)
        val body = screenSource.substring(sigStart, cacheCheck)
        assertTrue(
            "signature should include isHitsBased so cache invalidates on tab switch",
            body.contains("isHitsBased")
        )
    }

    @Test
    fun `ChartsContent suppresses the trend legend entry when hits-based`() {
        // The legend buildList has an `if (uiState.myTrend != null)` branch that
        // adds the trend legend item. That branch must also check !isHitsBased.
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
    fun `precision branch still allows auto-scaled axis (no unconditional axisMaximum)`() {
        // The fix should not force axisMaximum = 6f unconditionally; it must be
        // inside a branch that depends on isHitsBased.
        val idx = screenSource.indexOf("axisMaximum = 6f")
        if (idx >= 0) {
            val window = screenSource.substring(
                (idx - 200).coerceAtLeast(0),
                (idx + 100).coerceAtMost(screenSource.length)
            )
            assertTrue(
                "axisMaximum=6f must sit inside an isHitsBased branch, not run unconditionally",
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
    fun `ChartScatterChart still uses CombinedChart and ScatterDataSet`() {
        assertTrue(
            "CombinedChart usage must remain",
            screenSource.contains("CombinedChart")
        )
        assertTrue(
            "ScatterDataSet usage must remain",
            screenSource.contains("ScatterDataSet")
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
