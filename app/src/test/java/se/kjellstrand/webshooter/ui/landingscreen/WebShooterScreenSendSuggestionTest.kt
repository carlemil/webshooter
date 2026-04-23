package se.kjellstrand.webshooter.ui.landingscreen

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WebShooterScreenSendSuggestionTest {

    private val screenSource: String by lazy {
        File("src/main/java/se/kjellstrand/webshooter/ui/landingscreen/WebShooterScreen.kt").readText()
    }

    private val stringsXml: String by lazy {
        File("src/main/res/values/strings.xml").readText()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `strings_xml declares web_shooter_send_suggestion with label Skicka foerslag`() {
        assertTrue(
            "Expected a string resource named 'web_shooter_send_suggestion' with value 'Skicka förslag'",
            stringsXml.contains("name=\"web_shooter_send_suggestion\"") &&
                stringsXml.contains("Skicka förslag")
        )
    }

    @Test
    fun `strings_xml declares send_suggestion_email_subject`() {
        assertTrue(
            "Expected a string resource named 'send_suggestion_email_subject' with value 'Webshooter: förslag'",
            stringsXml.contains("name=\"send_suggestion_email_subject\"") &&
                stringsXml.contains("Webshooter: förslag")
        )
    }

    @Test
    fun `strings_xml declares send_suggestion_email_recipient as non-translatable`() {
        assertTrue(
            "Expected a translatable=\"false\" string resource named 'send_suggestion_email_recipient' with value 'erbsman@gmail.com'",
            stringsXml.contains("name=\"send_suggestion_email_recipient\"") &&
                stringsXml.contains("erbsman@gmail.com") &&
                stringsXml.contains("translatable=\"false\"")
        )
    }

    @Test
    fun `WebShooterScreen references the three suggestion string resources`() {
        assertTrue(
            "WebShooterScreen should reference R.string.web_shooter_send_suggestion",
            screenSource.contains("web_shooter_send_suggestion")
        )
        assertTrue(
            "WebShooterScreen should reference R.string.send_suggestion_email_subject",
            screenSource.contains("send_suggestion_email_subject")
        )
        assertTrue(
            "WebShooterScreen should reference R.string.send_suggestion_email_recipient",
            screenSource.contains("send_suggestion_email_recipient")
        )
    }

    @Test
    fun `WebShooterScreen fires an ACTION_SENDTO intent with mailto and EXTRA_SUBJECT`() {
        assertTrue(
            "WebShooterScreen should use Intent.ACTION_SENDTO to launch the email client",
            screenSource.contains("ACTION_SENDTO")
        )
        assertTrue(
            "WebShooterScreen should build a mailto: URI for the suggestion intent",
            screenSource.contains("\"mailto:")
        )
        assertTrue(
            "WebShooterScreen should attach EXTRA_SUBJECT so the email is pre-subjected",
            screenSource.contains("EXTRA_SUBJECT")
        )
    }

    @Test
    fun `settingsItems list does NOT include a routed entry for the suggestion`() {
        // The menu item must NOT be added to settingsItems (that list drives routed
        // navigation and the top-bar title lookup). Instead, it should be rendered
        // as a standalone NavigationDrawerItem after settingsItems.forEach {...}.
        // Guard against the easy-but-wrong approach of appending to settingsItems.
        val settingsListStart = screenSource.indexOf("val settingsItems = listOf(")
        assertTrue("settingsItems list should still be declared", settingsListStart >= 0)
        val listBody = screenSource.substring(
            settingsListStart,
            screenSource.indexOf(')', settingsListStart) + 1
        )
        assertFalse(
            "settingsItems must NOT include web_shooter_send_suggestion — it is not a routed entry",
            listBody.contains("web_shooter_send_suggestion")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `Settings section header and settingsItems rendering remain intact`() {
        assertTrue(
            "Settings section header should still be rendered",
            screenSource.contains("SectionHeader(stringResource(R.string.menu_group_settings))")
        )
        assertTrue(
            "settingsItems should still be iterated with MenuItem",
            screenSource.contains("settingsItems.forEach { MenuItem(it) }")
        )
    }

    @Test
    fun `existing Settings and Licenses menu items remain`() {
        assertTrue(
            "Settings route should still be in settingsItems",
            screenSource.contains("Screen.Settings.route")
        )
        assertTrue(
            "Licenses route should still be in settingsItems",
            screenSource.contains("Screen.Licenses.route")
        )
    }

    @Test
    fun `competition and stats drawer sections remain intact`() {
        assertTrue(
            "Competitions section header should still be rendered",
            screenSource.contains("SectionHeader(stringResource(R.string.menu_group_competitions))")
        )
        assertTrue(
            "Stats section header should still be rendered",
            screenSource.contains("SectionHeader(stringResource(R.string.menu_group_stats))")
        )
    }
}
