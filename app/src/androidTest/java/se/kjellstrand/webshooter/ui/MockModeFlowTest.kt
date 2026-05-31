package se.kjellstrand.webshooter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.mp.KoinPlatform
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.MockModeManager
import se.kjellstrand.webshooter.data.db.AppDatabase
import se.kjellstrand.webshooter.data.secure.SecurePrefs

/**
 * Drives the real Compose UI through the mockuser/mockpassword login path and
 * asserts that mock JSON (served by MockInterceptor) actually renders.
 *
 * Regression coverage for two bugs that previously made mock mode look broken:
 *   - MockInterceptor responses lacked a Content-Type header, so Ktor's
 *     ContentNegotiation refused to deserialize them.
 *   - MockModeManager.isMockMode is in-memory only; without persistence
 *     restart-after-login dropped back to the real backend.
 */
@RunWith(AndroidJUnit4::class)
class MockModeFlowTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun resetAppState() {
        val koin = KoinPlatform.getKoin()
        MockModeManager.isMockMode = false
        koin.get<AuthTokenManager>().clearToken()
        koin.get<SecurePrefs>().clearUsername()
        koin.get<SecurePrefs>().clearMockMode()
        koin.get<AppDatabase>().clearAllTables()

        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun teardown() {
        scenario.close()
    }

    @Test
    fun mockLogin_loadsCompetitionsList() {
        // Splash → login. Wait for the username field.
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodes(hasText("Användarnamn"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Användarnamn").performTextInput("mockuser")
        composeTestRule.onNodeWithText("Lösenord").performTextInput("mockpassword")
        // "Inloggning" is both the screen title and the button label; pick the
        // clickable one.
        composeTestRule.onNode(hasText("Inloggning") and hasClickAction()).performClick()

        // Landing → CompetitionsScreen. Wait for a known mock competition card
        // visible in the LazyColumn's initial viewport — see
        // app/src/main/res/raw/competitions.txt for the fixture.
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            composeTestRule
                .onAllNodes(hasText("Kretsfältskjutning F2 med nytt/nytt datum", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNode(hasText("Kretsfältskjutning F2 med nytt/nytt datum", substring = true))
            .assertIsDisplayed()

        // A second distinct fixture name — guards against the screen rendering
        // a single placeholder card from a partial parse.
        composeTestRule
            .onNode(hasText("Kretstävling i Magumprecision", substring = true))
            .assertIsDisplayed()
    }
}
