package se.kjellstrand.webshooter.ui

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives the drawer through every menu entry, asserting each landing screen
 * renders so that the drawer-item → composable wiring stays covered.
 *
 * The drawer has labels that repeat — "Inställningar" is both the section
 * header and a menu item, "Tävlingar" appears as section header + menu item
 * + top-bar title. All taps go through [clickFirstClickable] so we pick the
 * NavigationDrawerItem (the only one with onClick) instead of the static
 * label.
 */
@RunWith(AndroidJUnit4::class)
class DrawerNavigationTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        resetAppStateForTests()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun drawer_openCloseCycle_showsAllSectionHeadersAndItems() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()

        // Drawer title.
        composeTestRule.awaitText("Meny")

        // Asserting via awaitText (non-strict) since some labels repeat
        // between section headers and menu items.
        composeTestRule.awaitText("Statistik")
        composeTestRule.awaitText("Övrigt")
        composeTestRule.awaitText("Mina resultat")
        composeTestRule.awaitText("Resultattrender")
        composeTestRule.awaitText("Serieresultat")
        composeTestRule.awaitText("Föreningsstatistik")
        composeTestRule.awaitText("Licenser")
        composeTestRule.awaitText("Skicka förslag")
    }

    @Test
    fun drawer_navigatesToMyEntries() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Mina resultat")
        composeTestRule.awaitText("Sammanfattning", timeoutMillis = 20_000)
    }

    @Test
    fun drawer_navigatesToCharts() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Resultattrender")
        composeTestRule.awaitText("Snittpoäng", substring = true, timeoutMillis = 20_000)
    }

    @Test
    fun drawer_navigatesToSeriesPoints() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Serieresultat")
        composeTestRule.awaitText("Serieresultat", timeoutMillis = 20_000)
    }

    @Test
    fun drawer_navigatesToClubStats() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Föreningsstatistik")
        composeTestRule.awaitText("Föreningsstatistik", timeoutMillis = 20_000)
    }

    @Test
    fun drawer_navigatesToClub() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Förening")
        // The club info tab is "Information" — landing pane.
        composeTestRule.awaitText("Information", timeoutMillis = 20_000)
    }

    @Test
    fun drawer_navigatesToSettings() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Inställningar")
        composeTestRule.awaitText("Personlig information", timeoutMillis = 20_000)
    }

    @Test
    fun drawer_navigatesToLicenses() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Licenser")
        composeTestRule.awaitText("Apache License 2.0")
    }
}
