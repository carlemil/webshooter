package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import org.junit.Assert.assertEquals
import org.junit.Test

class TrendsTabKeyTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `trendsTabKeyFor returns magnumprecision when competitionTypeName is Magnumprecision`() {
        assertEquals(
            "magnumprecision",
            trendsTabKeyFor("Magnumprecision", "precision")
        )
    }

    @Test
    fun `trendsTabKeyFor returns resultsType when competitionTypeName is some other name`() {
        assertEquals("precision", trendsTabKeyFor("Precision", "precision"))
        assertEquals("field", trendsTabKeyFor("Fältskytte", "field"))
        assertEquals("military", trendsTabKeyFor("Militär", "military"))
    }

    @Test
    fun `trendsTabKeyFor returns resultsType when competitionTypeName is null`() {
        assertEquals("precision", trendsTabKeyFor(null, "precision"))
        assertEquals("field", trendsTabKeyFor(null, "field"))
    }

    @Test
    fun `MAGNUMPRECISION_TAB_KEY constant equals magnumprecision string`() {
        assertEquals("magnumprecision", MAGNUMPRECISION_TAB_KEY)
    }

    @Test
    fun `MAGNUMPRECISION_COMPETITION_TYPE_NAME constant equals API value`() {
        assertEquals("Magnumprecision", MAGNUMPRECISION_COMPETITION_TYPE_NAME)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `trendsTabKeyFor is case-sensitive for the discriminator name`() {
        // Spec is explicit equality, so a lowercase variant must NOT trigger the magnumprecision bucket.
        assertEquals("precision", trendsTabKeyFor("magnumprecision", "precision"))
        assertEquals("precision", trendsTabKeyFor("MAGNUMPRECISION", "precision"))
    }

    @Test
    fun `trendsTabKeyFor passes resultsType through unchanged for unknown types`() {
        assertEquals("pointfield", trendsTabKeyFor(null, "pointfield"))
        assertEquals("custom", trendsTabKeyFor("Other", "custom"))
    }

    @Test
    fun `trendsTabKeyFor returns resultsType when competitionTypeName is empty`() {
        assertEquals("precision", trendsTabKeyFor("", "precision"))
    }
}
