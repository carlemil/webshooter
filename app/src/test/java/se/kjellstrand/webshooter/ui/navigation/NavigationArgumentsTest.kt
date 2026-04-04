package se.kjellstrand.webshooter.ui.navigation

import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NavigationArgumentsTest {

    // --- requireLong ---

    @Test
    fun `requireLong returns value when argument is present`() {
        val bundle = Bundle().apply { putLong("competitionId", 42L) }
        assertEquals(42L, NavigationArguments.requireLong(bundle, "competitionId"))
    }

    @Test
    fun `requireLong handles Long MAX_VALUE`() {
        val bundle = Bundle().apply { putLong("competitionId", Long.MAX_VALUE) }
        assertEquals(Long.MAX_VALUE, NavigationArguments.requireLong(bundle, "competitionId"))
    }

    @Test
    fun `requireLong handles zero`() {
        val bundle = Bundle().apply { putLong("competitionId", 0L) }
        assertEquals(0L, NavigationArguments.requireLong(bundle, "competitionId"))
    }

    @Test
    fun `requireLong handles negative value`() {
        val bundle = Bundle().apply { putLong("competitionId", -5L) }
        assertEquals(-5L, NavigationArguments.requireLong(bundle, "competitionId"))
    }

    @Test
    fun `requireLong throws when bundle is null`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(null, "competitionId")
        }
        assertEquals(
            "Navigation arguments bundle is null, missing required argument: competitionId",
            exception.message
        )
    }

    @Test
    fun `requireLong throws when key is missing`() {
        val bundle = Bundle()
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(bundle, "competitionId")
        }
        assertEquals("Missing required navigation argument: competitionId", exception.message)
    }

    @Test
    fun `requireLong throws when key is wrong name`() {
        val bundle = Bundle().apply { putLong("otherId", 42L) }
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(bundle, "competitionId")
        }
        assertEquals("Missing required navigation argument: competitionId", exception.message)
    }

    // --- requireInt ---

    @Test
    fun `requireInt returns value when argument is present`() {
        val bundle = Bundle().apply { putInt("competitionId", 99) }
        assertEquals(99, NavigationArguments.requireInt(bundle, "competitionId"))
    }

    @Test
    fun `requireInt handles Int MAX_VALUE`() {
        val bundle = Bundle().apply { putInt("competitionId", Int.MAX_VALUE) }
        assertEquals(Int.MAX_VALUE, NavigationArguments.requireInt(bundle, "competitionId"))
    }

    @Test
    fun `requireInt handles zero`() {
        val bundle = Bundle().apply { putInt("competitionId", 0) }
        assertEquals(0, NavigationArguments.requireInt(bundle, "competitionId"))
    }

    @Test
    fun `requireInt throws when bundle is null`() {
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireInt(null, "competitionId")
        }
    }

    @Test
    fun `requireInt throws when key is missing`() {
        val bundle = Bundle()
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireInt(bundle, "competitionId")
        }
    }

    // --- requireString ---

    @Test
    fun `requireString returns value when argument is present`() {
        val bundle = Bundle().apply { putString("resultsType", "FIELD") }
        assertEquals("FIELD", NavigationArguments.requireString(bundle, "resultsType"))
    }

    @Test
    fun `requireString handles empty string`() {
        val bundle = Bundle().apply { putString("resultsType", "") }
        assertEquals("", NavigationArguments.requireString(bundle, "resultsType"))
    }

    @Test
    fun `requireString handles special characters`() {
        val bundle = Bundle().apply { putString("name", "Tävling & Test / 2026") }
        assertEquals("Tävling & Test / 2026", NavigationArguments.requireString(bundle, "name"))
    }

    @Test
    fun `requireString throws when bundle is null`() {
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireString(null, "resultsType")
        }
    }

    @Test
    fun `requireString throws when key is missing`() {
        val bundle = Bundle()
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireString(bundle, "resultsType")
        }
    }

    @Test
    fun `requireString throws when value is null`() {
        val bundle = Bundle().apply { putString("resultsType", null) }
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireString(bundle, "resultsType")
        }
    }

    // --- Bug reproduction: the old ?: -1L pattern ---

    @Test
    fun `old pattern silently returns negative one when argument is missing`() {
        // This test demonstrates the bug: the old code used ?: -1L
        // which silently passes an invalid ID downstream
        val bundle = Bundle()
        val oldResult = bundle.getLong("competitionId", -1L)
        assertEquals(-1L, oldResult) // Bug: -1L is not a valid competition ID

        // The new code should throw instead
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(bundle, "competitionId")
        }
    }

    @Test
    fun `old pattern silently returns negative one when bundle is null`() {
        // Demonstrates: arguments?.getLong("competitionId") ?: -1L
        val arguments: Bundle? = null
        val oldResult = arguments?.getLong("competitionId") ?: -1L
        assertEquals(-1L, oldResult)

        // The new code should throw instead
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(arguments, "competitionId")
        }
    }
}
