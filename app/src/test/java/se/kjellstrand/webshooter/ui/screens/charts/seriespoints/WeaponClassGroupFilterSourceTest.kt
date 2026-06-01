package se.kjellstrand.webshooter.ui.screens.charts.seriespoints

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source-text tests for `WeaponClassGroupFilter.kt`. Stays on the JVM
 * because it reads files from the source tree — the runtime-logic tests
 * for `WeaponClassGroup` itself live in `shared/src/commonTest/`.
 */
class WeaponClassGroupFilterSourceTest {

    @Test
    fun `WeaponClassGroupFilter renders only groups in availableGroups`() {
        val source = File(
            "../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/common/WeaponClassGroupFilter.kt"
        ).readText()
        assertFalse(
            "Filter must not iterate WeaponClassGroup.values() any more (would render ghost buttons for 13 groups)",
            Regex("""WeaponClassGroup\s*\.\s*values\s*\(\s*\)""").containsMatchIn(source)
        )
        assertTrue(
            "Filter must iterate availableGroups, sorted by ordinal for stable layout",
            Regex("""availableGroups\s*\.\s*sortedBy\s*\{\s*it\.ordinal\s*\}""").containsMatchIn(source)
        )
    }

    @Test
    fun `WeaponClassGroupFilter no longer applies disabled state styling`() {
        val source = File(
            "../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/common/WeaponClassGroupFilter.kt"
        ).readText()
        assertFalse(
            "Filter must not branch on `enabled` (only enabled groups are rendered now)",
            Regex("""enabled\s*=\s*group\s+in\s+availableGroups""").containsMatchIn(source)
        )
    }
}
