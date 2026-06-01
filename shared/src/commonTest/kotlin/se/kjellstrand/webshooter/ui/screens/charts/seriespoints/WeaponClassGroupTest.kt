package se.kjellstrand.webshooter.ui.screens.charts.seriespoints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup

class WeaponClassGroupTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun enum_has_R_group_plus_M1_through_M9() {
        val names = WeaponClassGroup.values().map { it.name }.toSet()
        assertTrue("R" in names, "R group must exist")
        for (i in 1..9) {
            assertTrue("M$i" in names, "M$i group must exist")
        }
    }

    @Test
    fun enum_order_is_A_B_C_R_M1_through_M9() {
        val expected = listOf("A", "B", "C", "R", "M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9")
        val actual = WeaponClassGroup.values().map { it.name }
        assertEquals(expected, actual)
    }

    @Test
    fun prefix_property_returns_String_not_Char() {
        // After the fix, prefix must be a String (so M1 etc. can be 2 chars).
        // We assert by calling startsWith on it — only String has that.
        val prefix: Any = WeaponClassGroup.A.prefix
        assertTrue(prefix is String, "prefix should be a String")
    }

    @Test
    fun R_matches_R1_R2_R3_weapon_classes() {
        val r = WeaponClassGroup.valueOf("R")
        assertTrue(r.matches("R1"))
        assertTrue(r.matches("R2"))
        assertTrue(r.matches("R3"))
        assertTrue(r.matches("R"))
    }

    @Test
    fun R_does_not_match_weapons_that_merely_contain_an_R_later_in_the_string() {
        val r = WeaponClassGroup.valueOf("R")
        assertFalse(r.matches("AR15"))
        assertFalse(r.matches("BR"))
    }

    @Test
    fun M7_matches_only_M7_prefixed_weapon_classes() {
        val m7 = WeaponClassGroup.valueOf("M7")
        assertTrue(m7.matches("M7"))
        assertTrue(m7.matches("M7A"))
        assertFalse(m7.matches("M1"), "M1 should not match M7")
        assertFalse(m7.matches("M"), "M must not match M7 alone")
        assertFalse(m7.matches(""))
    }

    @Test
    fun each_M_group_matches_only_its_own_prefix() {
        for (i in 1..9) {
            val group = WeaponClassGroup.valueOf("M$i")
            assertTrue(group.matches("M$i"), "M$i should match M$i sample")
            // Pick a different M index to verify it doesn't cross-match.
            val other = if (i == 1) 2 else 1
            assertFalse(group.matches("M$other"), "M$i must not match M$other")
        }
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun enum_still_has_A_B_C_groups() {
        val names = WeaponClassGroup.values().map { it.name }.toSet()
        assertTrue("A" in names, "A must remain")
        assertTrue("B" in names, "B must remain")
        assertTrue("C" in names, "C must remain")
    }

    @Test
    fun A_matches_A_prefixed_weapon_classes() {
        val a = WeaponClassGroup.valueOf("A")
        assertTrue(a.matches("A"))
        assertTrue(a.matches("A2"))
    }

    @Test
    fun matches_is_case_insensitive() {
        val a = WeaponClassGroup.valueOf("A")
        assertTrue(a.matches("a"), "lowercase a should still match")
        assertTrue(a.matches("a2"), "lowercase a2 should still match")
    }

    @Test
    fun matches_returns_false_on_empty_string() {
        val a = WeaponClassGroup.valueOf("A")
        assertFalse(a.matches(""))
    }

    @Test
    fun WeaponClassGroup_is_declared_in_the_clubstats_package() {
        // Guard against accidental package moves — other code imports from here.
        // Uses KMP-portable KClass.qualifiedName (Java reflection unavailable on iOS).
        assertEquals(
            "se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup",
            WeaponClassGroup::class.qualifiedName
        )
        assertNotNull(WeaponClassGroup::class)
    }
}
