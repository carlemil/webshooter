package se.kjellstrand.webshooter.ui.navigation

import android.app.Application
import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// android.os.Bundle is an Android-SDK stub at JVM-test time; Robolectric
// provides a real implementation. We override the application class to a
// bare `android.app.Application` so Robolectric doesn't boot
// `ShooterApplication`, which would double-start Koin and conflict with
// other tests in this source set.
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class NavigationArgumentsTest {

    // --- requireLong ---

    @Test
    fun requireLong_returns_value_when_argument_is_present() {
        val bundle = Bundle().apply { putLong("competitionId", 42L) }
        assertEquals(42L, NavigationArguments.requireLong(bundle, "competitionId"))
    }

    @Test
    fun requireLong_handles_Long_MAX_VALUE() {
        val bundle = Bundle().apply { putLong("competitionId", Long.MAX_VALUE) }
        assertEquals(Long.MAX_VALUE, NavigationArguments.requireLong(bundle, "competitionId"))
    }

    @Test
    fun requireLong_handles_zero() {
        val bundle = Bundle().apply { putLong("competitionId", 0L) }
        assertEquals(0L, NavigationArguments.requireLong(bundle, "competitionId"))
    }

    @Test
    fun requireLong_handles_negative_value() {
        val bundle = Bundle().apply { putLong("competitionId", -5L) }
        assertEquals(-5L, NavigationArguments.requireLong(bundle, "competitionId"))
    }

    @Test
    fun requireLong_throws_when_bundle_is_null() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(null, "competitionId")
        }
        assertEquals(
            "Navigation arguments bundle is null, missing required argument: competitionId",
            exception.message
        )
    }

    @Test
    fun requireLong_throws_when_key_is_missing() {
        val bundle = Bundle()
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(bundle, "competitionId")
        }
        assertEquals("Missing required navigation argument: competitionId", exception.message)
    }

    @Test
    fun requireLong_throws_when_key_is_wrong_name() {
        val bundle = Bundle().apply { putLong("otherId", 42L) }
        val exception = assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(bundle, "competitionId")
        }
        assertEquals("Missing required navigation argument: competitionId", exception.message)
    }

    // --- requireInt ---

    @Test
    fun requireInt_returns_value_when_argument_is_present() {
        val bundle = Bundle().apply { putInt("competitionId", 99) }
        assertEquals(99, NavigationArguments.requireInt(bundle, "competitionId"))
    }

    @Test
    fun requireInt_handles_Int_MAX_VALUE() {
        val bundle = Bundle().apply { putInt("competitionId", Int.MAX_VALUE) }
        assertEquals(Int.MAX_VALUE, NavigationArguments.requireInt(bundle, "competitionId"))
    }

    @Test
    fun requireInt_handles_zero() {
        val bundle = Bundle().apply { putInt("competitionId", 0) }
        assertEquals(0, NavigationArguments.requireInt(bundle, "competitionId"))
    }

    @Test
    fun requireInt_throws_when_bundle_is_null() {
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireInt(null, "competitionId")
        }
    }

    @Test
    fun requireInt_throws_when_key_is_missing() {
        val bundle = Bundle()
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireInt(bundle, "competitionId")
        }
    }

    // --- requireString ---

    @Test
    fun requireString_returns_value_when_argument_is_present() {
        val bundle = Bundle().apply { putString("resultsType", "FIELD") }
        assertEquals("FIELD", NavigationArguments.requireString(bundle, "resultsType"))
    }

    @Test
    fun requireString_handles_empty_string() {
        val bundle = Bundle().apply { putString("resultsType", "") }
        assertEquals("", NavigationArguments.requireString(bundle, "resultsType"))
    }

    @Test
    fun requireString_handles_special_characters() {
        val bundle = Bundle().apply { putString("name", "Tävling & Test / 2026") }
        assertEquals("Tävling & Test / 2026", NavigationArguments.requireString(bundle, "name"))
    }

    @Test
    fun requireString_throws_when_bundle_is_null() {
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireString(null, "resultsType")
        }
    }

    @Test
    fun requireString_throws_when_key_is_missing() {
        val bundle = Bundle()
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireString(bundle, "resultsType")
        }
    }

    @Test
    fun requireString_throws_when_value_is_null() {
        val bundle = Bundle().apply { putString("resultsType", null) }
        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireString(bundle, "resultsType")
        }
    }

    // --- Bug reproduction: the old ?: -1L pattern ---

    @Test
    fun old_pattern_silently_returns_negative_one_when_argument_is_missing() {
        // The old code used `?: -1L`, silently passing an invalid ID downstream.
        val bundle = Bundle()
        val oldResult = bundle.getLong("competitionId", -1L)
        assertEquals(-1L, oldResult)

        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(bundle, "competitionId")
        }
    }

    @Test
    fun old_pattern_silently_returns_negative_one_when_bundle_is_null() {
        // Demonstrates: arguments?.getLong("competitionId") ?: -1L
        val arguments: Bundle? = null
        val oldResult = arguments?.getLong("competitionId") ?: -1L
        assertEquals(-1L, oldResult)

        assertThrows(IllegalArgumentException::class.java) {
            NavigationArguments.requireLong(arguments, "competitionId")
        }
    }
}
