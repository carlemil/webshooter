package se.kjellstrand.webshooter.ui

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the per-competition buttons on the competitions list and the
 * detail screens they navigate to.
 *
 * The first viewport renders cards for two competitions that are *open for
 * signup*, so the always-present buttons we drive against are "Deltagare"
 * and "Patruller" (the "Resultat" button only appears on completed
 * competitions). Back buttons on detail screens use contentDescription
 * "Back" (the default ArrowBack label).
 */
@RunWith(AndroidJUnit4::class)
class CompetitionDetailsNavigationTest {

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

    private fun clickFirstButton(label: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodes(hasText(label) and hasClickAction())
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onAllNodes(hasText(label) and hasClickAction())[0]
            .performClick()
    }

    private fun pressBack() {
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()
    }

    @Test
    fun competition_participantsButton_navigatesToSignupsList() {
        composeTestRule.performMockLogin()
        clickFirstButton("Deltagare")
        composeTestRule.awaitContentDescription("Back")
        pressBack()
        composeTestRule.awaitText("Tävlingar")
    }

    @Test
    fun competition_patrolsButton_navigatesToPatrolsScreen() {
        composeTestRule.performMockLogin()
        clickFirstButton("Patruller")
        composeTestRule.awaitContentDescription("Back")
        pressBack()
        composeTestRule.awaitText("Tävlingar")
    }

    @Test
    fun competition_signupButton_navigatesToSignupScreen() {
        composeTestRule.performMockLogin()
        clickFirstButton("Anmälan")
        // SignupScreen uses Swedish "Tillbaka" — see SignupScreen.kt's TopBar.
        composeTestRule.awaitContentDescription("Tillbaka")
        composeTestRule.onNodeWithContentDescription("Tillbaka").performClick()
        composeTestRule.awaitText("Tävlingar")
    }

    @Test
    fun competitionCard_expandButton_revealsContactInfoSection() {
        composeTestRule.performMockLogin()
        // The expand button's contentDescription toggles between "Expandera"
        // and "Komprimera". Click the first "Expandera".
        composeTestRule.awaitContentDescription("Expandera")
        composeTestRule
            .onAllNodes(hasContentDescription("Expandera"))[0]
            .performClick()
        // The expanded section shows "Kontaktinformation" card.
        composeTestRule.awaitText("Kontaktinformation", timeoutMillis = 10_000)
    }

    @Test
    fun competitionsList_filterFab_opensStatusBottomSheet() {
        composeTestRule.performMockLogin()
        // The status-filter FAB has contentDescription "Open Filters".
        composeTestRule.onNodeWithContentDescription("Open Filters").performClick()
        // The bottom sheet's title is "Status" (filter-by-status).
        composeTestRule.awaitText("Status", timeoutMillis = 10_000)
        // "Klar" closes the sheet.
        composeTestRule
            .onAllNodes(hasText("Klar") and hasClickAction())[0]
            .performClick()
    }
}
