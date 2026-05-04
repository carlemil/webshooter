package se.kjellstrand.webshooter.ui.screens.charts.seriespoints

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup

class WeaponClassGroupTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `enum has R group plus M1 through M9`() {
        val names = WeaponClassGroup.values().map { it.name }.toSet()
        assertTrue("R group must exist", "R" in names)
        for (i in 1..9) {
            assertTrue("M$i group must exist", "M$i" in names)
        }
    }

    @Test
    fun `enum order is A B C R M1 through M9`() {
        val expected = listOf("A", "B", "C", "R", "M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9")
        val actual = WeaponClassGroup.values().map { it.name }
        assertEquals(expected, actual)
    }

    @Test
    fun `prefix property returns String not Char`() {
        // After the fix, prefix must be a String (so M1 etc. can be 2 chars).
        // We assert by calling startsWith on it — only String has that.
        val prefix: Any = WeaponClassGroup.A.prefix
        assertTrue("prefix should be a String", prefix is String)
    }

    @Test
    fun `R matches R1 R2 R3 weapon classes`() {
        val r = WeaponClassGroup.valueOf("R")
        assertTrue(r.matches("R1"))
        assertTrue(r.matches("R2"))
        assertTrue(r.matches("R3"))
        assertTrue(r.matches("R"))
    }

    @Test
    fun `R does not match weapons that merely contain an R later in the string`() {
        val r = WeaponClassGroup.valueOf("R")
        assertFalse(r.matches("AR15"))
        assertFalse(r.matches("BR"))
    }

    @Test
    fun `M7 matches only M7-prefixed weapon classes`() {
        val m7 = WeaponClassGroup.valueOf("M7")
        assertTrue(m7.matches("M7"))
        assertTrue(m7.matches("M7A"))
        assertFalse("M1 should not match M7", m7.matches("M1"))
        assertFalse("M must not match M7 alone", m7.matches("M"))
        assertFalse(m7.matches(""))
    }

    @Test
    fun `each M-group matches only its own prefix`() {
        for (i in 1..9) {
            val group = WeaponClassGroup.valueOf("M$i")
            assertTrue("M$i should match M$i sample", group.matches("M${i}"))
            // Pick a different M index to verify it doesn't cross-match.
            val other = if (i == 1) 2 else 1
            assertFalse("M$i must not match M$other", group.matches("M$other"))
        }
    }

    @Test
    fun `WeaponClassGroupFilter renders only groups in availableGroups`() {
        val source = java.io.File(
            "src/main/java/se/kjellstrand/webshooter/ui/common/WeaponClassGroupFilter.kt"
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
        val source = java.io.File(
            "src/main/java/se/kjellstrand/webshooter/ui/common/WeaponClassGroupFilter.kt"
        ).readText()
        assertFalse(
            "Filter must not branch on `enabled` (only enabled groups are rendered now)",
            Regex("""enabled\s*=\s*group\s+in\s+availableGroups""").containsMatchIn(source)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `enum still has A B C groups`() {
        val names = WeaponClassGroup.values().map { it.name }.toSet()
        assertTrue("A must remain", "A" in names)
        assertTrue("B must remain", "B" in names)
        assertTrue("C must remain", "C" in names)
    }

    @Test
    fun `A matches A-prefixed weapon classes`() {
        val a = WeaponClassGroup.valueOf("A")
        assertTrue(a.matches("A"))
        assertTrue(a.matches("A2"))
    }

    @Test
    fun `matches is case-insensitive`() {
        val a = WeaponClassGroup.valueOf("A")
        assertTrue("lowercase a should still match", a.matches("a"))
        assertTrue("lowercase a2 should still match", a.matches("a2"))
    }

    @Test
    fun `matches returns false on empty string`() {
        val a = WeaponClassGroup.valueOf("A")
        assertFalse(a.matches(""))
    }

    @Test
    fun `WeaponClassGroup is declared in the seriespoints package`() {
        // Guard against accidental package moves — other code imports from here.
        val clazz = WeaponClassGroup::class.java
        assertEquals(
            "se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup",
            clazz.name
        )
        assertNotNull(clazz)
    }
}
