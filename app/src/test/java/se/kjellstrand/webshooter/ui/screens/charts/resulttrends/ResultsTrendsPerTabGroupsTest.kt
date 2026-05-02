package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.WeaponClassGroup
import java.io.File

/**
 * Tests for the per-tab availableGroups + default selectedGroup behavior.
 * Helpers (groupsForTab, defaultGroupForTab) are tested directly; the
 * ViewModel wiring is verified via source-regex tests because the existing
 * ViewModel tests in this codebase use that same pattern (no Hilt fixture).
 */
class ResultsTrendsPerTabGroupsTest {

    private val uiStateFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsUiState.kt")
    private val vmImplFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/resulttrends/ResultsTrendsViewModelImpl.kt")

    /** Look up a top-level Kotlin function by name from the package's compiled file class. */
    private fun callTopLevel(funName: String, vararg args: Any?): Any? {
        val clazz = Class.forName(
            "se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsUiStateKt"
        )
        val method = clazz.declaredMethods.find { it.name == funName }
            ?: error("Top-level fun $funName not found on ResultsTrendsUiStateKt")
        return method.invoke(null, *args)
    }

    @Suppress("UNCHECKED_CAST")
    private fun groupsForTab(tabKey: String): Set<WeaponClassGroup> =
        callTopLevel("groupsForTab", tabKey) as Set<WeaponClassGroup>

    private fun defaultGroupForTab(tabKey: String): WeaponClassGroup =
        callTopLevel("defaultGroupForTab", tabKey) as WeaponClassGroup

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `groupsForTab returns A B C R for field`() {
        val expected = setOf(
            WeaponClassGroup.A, WeaponClassGroup.B,
            WeaponClassGroup.C, WeaponClassGroup.R
        )
        assertEquals(expected, groupsForTab("field"))
    }

    @Test
    fun `groupsForTab returns M1 through M9 for magnumprecision`() {
        val expected = setOf(
            WeaponClassGroup.M1, WeaponClassGroup.M2, WeaponClassGroup.M3,
            WeaponClassGroup.M4, WeaponClassGroup.M5, WeaponClassGroup.M6,
            WeaponClassGroup.M7, WeaponClassGroup.M8, WeaponClassGroup.M9
        )
        assertEquals(expected, groupsForTab(MAGNUMPRECISION_TAB_KEY))
    }

    @Test
    fun `groupsForTab returns A B C for precision and military and other`() {
        val abc = setOf(WeaponClassGroup.A, WeaponClassGroup.B, WeaponClassGroup.C)
        assertEquals(abc, groupsForTab("precision"))
        assertEquals(abc, groupsForTab("military"))
        assertEquals(abc, groupsForTab("anything-else"))
    }

    @Test
    fun `defaultGroupForTab returns M7 for magnumprecision`() {
        assertEquals(WeaponClassGroup.M7, defaultGroupForTab(MAGNUMPRECISION_TAB_KEY))
    }

    @Test
    fun `defaultGroupForTab returns C for every other tab`() {
        assertEquals(WeaponClassGroup.C, defaultGroupForTab("precision"))
        assertEquals(WeaponClassGroup.C, defaultGroupForTab("field"))
        assertEquals(WeaponClassGroup.C, defaultGroupForTab("military"))
        assertEquals(WeaponClassGroup.C, defaultGroupForTab(""))
    }

    @Test
    fun `selectTab resets selectedGroup to defaultGroupForTab and updates availableGroups`() {
        val source = vmImplFile.readText()
        val selectTabBlock = Regex(
            """fun\s+selectTab\s*\([^)]*\)\s*\{[\s\S]*?\n\s*\}"""
        ).find(source)?.value
            ?: error("Could not find selectTab function body")
        assertTrue(
            "selectTab must compute groupsForTab(resultsType)",
            Regex("""groupsForTab\s*\(""").containsMatchIn(selectTabBlock)
        )
        assertTrue(
            "selectTab must set availableGroups in the copy",
            Regex("""availableGroups\s*=""").containsMatchIn(selectTabBlock)
        )
        assertTrue(
            "selectTab must reset selectedGroup via defaultGroupForTab",
            Regex("""selectedGroup\s*=\s*defaultGroupForTab\s*\(""").containsMatchIn(selectTabBlock)
        )
    }

    @Test
    fun `loadChartData wires availableGroups and selectedGroup using helpers`() {
        val source = vmImplFile.readText()
        assertTrue(
            "loadChartData must compute groups via groupsForTab(selectedType)",
            Regex("""groupsForTab\s*\(\s*selectedType\s*\)""").containsMatchIn(source)
        )
        // The current group is preserved if still valid; otherwise reset to the default.
        assertTrue(
            "loadChartData must keep the current selectedGroup if it remains valid for the new tab",
            Regex("""\bselectedGroup\b[\s\S]{0,80}\bin\b[\s\S]{0,80}""").containsMatchIn(source) ||
                Regex("""selectedGroup[\s\S]{0,80}defaultGroupForTab""").containsMatchIn(source)
        )
        assertTrue(
            "loadChartData copy() must include availableGroups",
            Regex("""copy\s*\(([\s\S]*?availableGroups\s*=)""").containsMatchIn(source)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ChartsUiState still has selectedGroup and availableGroups fields`() {
        val source = uiStateFile.readText()
        assertTrue(source.contains("val selectedGroup: WeaponClassGroup?"))
        assertTrue(source.contains("val availableGroups: Set<WeaponClassGroup>"))
    }

    @Test
    fun `selectTab still updates selectedResultsType`() {
        val source = vmImplFile.readText()
        val selectTabBlock = Regex(
            """fun\s+selectTab\s*\([^)]*\)\s*\{[\s\S]*?\n\s*\}"""
        ).find(source)?.value
            ?: error("Could not find selectTab function body")
        assertTrue(
            "selectTab must still set selectedResultsType to the incoming type",
            Regex("""selectedResultsType\s*=""").containsMatchIn(selectTabBlock)
        )
    }

    @Test
    fun `WeaponClassGroup R and M7 enum entries exist`() {
        // These are required by the helpers — guard against accidental removal.
        assertNotNull(WeaponClassGroup.valueOf("R"))
        assertNotNull(WeaponClassGroup.valueOf("M7"))
    }

    @Test
    fun `groupsForTab never returns an empty set`() {
        assertFalse(groupsForTab("precision").isEmpty())
        assertFalse(groupsForTab("field").isEmpty())
        assertFalse(groupsForTab(MAGNUMPRECISION_TAB_KEY).isEmpty())
        assertFalse(groupsForTab("unknown").isEmpty())
    }
}
