package se.kjellstrand.webshooter.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ScreenChartsRouteTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun Charts_screen_object_exists_in_Screen_sealed_class() {
        // Direct reference acts as a compile-time + runtime guard. If
        // Screen.Charts ever gets removed, this file fails to compile —
        // a stronger contract than the Class.forName check this replaces.
        assertNotNull(Screen.Charts, "Screen.Charts should exist as an inner object")
    }

    @Test
    fun Charts_screen_route_is_charts() {
        assertEquals("charts", Screen.Charts.route)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun CompetitionsList_route_is_competitions() {
        assertEquals("competitions", Screen.CompetitionsList.route)
    }

    @Test
    fun MyEntries_route_is_my_entries() {
        assertEquals("my_entries", Screen.MyEntries.route)
    }

    @Test
    fun Club_route_is_club() {
        assertEquals("club", Screen.Club.route)
    }

    @Test
    fun Settings_route_is_settings() {
        assertEquals("settings", Screen.Settings.route)
    }
}
