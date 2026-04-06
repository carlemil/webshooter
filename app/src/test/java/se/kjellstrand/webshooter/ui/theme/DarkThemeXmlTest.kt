package se.kjellstrand.webshooter.ui.theme

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DarkThemeXmlTest {

    private val nightThemeFile = File("src/main/res/values-night/themes.xml")
    private val dayThemeFile = File("src/main/res/values/themes.xml")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `values-night themes xml exists`() {
        assertTrue(
            "values-night/themes.xml should exist for system-level dark theme consistency",
            nightThemeFile.exists()
        )
    }

    @Test
    fun `night theme uses dark parent theme`() {
        if (!nightThemeFile.exists()) {
            fail("values-night/themes.xml does not exist")
            return
        }
        val content = nightThemeFile.readText()
        assertTrue(
            "Night theme should use a dark parent theme (DayNight or Material Dark)",
            content.contains("Theme.Material") && content.contains("NoActionBar")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `day theme file exists`() {
        assertTrue(dayThemeFile.exists())
    }

    @Test
    fun `day theme defines Theme WebShooter`() {
        val content = dayThemeFile.readText()
        assertTrue(content.contains("Theme.WebShooter"))
    }

    @Test
    fun `day theme has NoActionBar parent`() {
        val content = dayThemeFile.readText()
        assertTrue(content.contains("NoActionBar"))
    }
}
