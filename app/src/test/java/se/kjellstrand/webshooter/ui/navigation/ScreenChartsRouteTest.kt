package se.kjellstrand.webshooter.ui.navigation

import org.junit.Test
import org.junit.Assert.*

class ScreenChartsRouteTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `Charts screen object exists in Screen sealed class`() {
        val clazz = try {
            Class.forName("se.kjellstrand.webshooter.ui.navigation.Screen\$Charts")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("Screen.Charts should exist as an inner object", clazz)
    }

    @Test
    fun `Charts screen route is charts`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.navigation.Screen\$Charts")
        val instanceField = clazz.getField("INSTANCE")
        val instance = instanceField.get(null) as Screen
        assertEquals("charts", instance.route)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionsList route is competitions`() {
        assertEquals("competitions", Screen.CompetitionsList.route)
    }

    @Test
    fun `MyEntries route is my_entries`() {
        assertEquals("my_entries", Screen.MyEntries.route)
    }

    @Test
    fun `Club route is club`() {
        assertEquals("club", Screen.Club.route)
    }

    @Test
    fun `Settings route is settings`() {
        assertEquals("settings", Screen.Settings.route)
    }
}
