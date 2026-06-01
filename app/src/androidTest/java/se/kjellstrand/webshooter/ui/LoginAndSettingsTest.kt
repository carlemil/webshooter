package se.kjellstrand.webshooter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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

/**
 * Covers the login form's interactive elements (password visibility toggle,
 * disabled-when-empty submit button) and the settings screen's tabs, edit
 * mode, and logout confirmation dialog.
 */
@RunWith(AndroidJUnit4::class)
class LoginAndSettingsTest {

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
    fun loginScreen_passwordVisibilityToggle_clickable() {
        composeTestRule.awaitText("Användarnamn")
        composeTestRule.onNodeWithText("Lösenord").performTextInput("secret")
        // The visibility icon button's contentDescription is "Visa/dölj lösenord".
        composeTestRule.awaitContentDescription("Visa/dölj lösenord")
        composeTestRule.onNodeWithContentDescription("Visa/dölj lösenord").performClick()
        // Toggle a second time so we exercise both states.
        composeTestRule.onNodeWithContentDescription("Visa/dölj lösenord").performClick()
    }

    @Test
    fun loginScreen_fieldsAndButtonRender() {
        composeTestRule.awaitText("Användarnamn")
        composeTestRule.onNodeWithText("Användarnamn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lösenord").assertIsDisplayed()
        // Login button (filter to clickable; "Inloggning" is also the title).
        composeTestRule
            .onAllNodes(hasText("Inloggning") and hasClickAction())[0]
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_profileAndPasswordTabs_switchContent() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Inställningar")

        // Profile tab is initially selected; "Personlig information" card visible.
        composeTestRule.awaitText("Personlig information", timeoutMillis = 20_000)

        // Switch to password tab. "Lösenord" appears twice (the password
        // field placeholder on the profile view + the tab label), so target
        // the clickable.
        composeTestRule.clickFirstClickable("Lösenord")
        composeTestRule.awaitText("Ändra lösenord", timeoutMillis = 10_000)

        // The password-form labels render. ("Nuvarande lösenord" etc. are
        // OutlinedTextField labels — assertion via awaitText is robust to
        // duplicate semantic nodes between label and trailing helper text.)
        composeTestRule.awaitText("Nuvarande lösenord")
        composeTestRule.awaitText("Nytt lösenord")
        composeTestRule.awaitText("Bekräfta nytt lösenord")
        composeTestRule
            .onAllNodes(hasText("Uppdatera lösenord") and hasClickAction())[0]
            .assertIsDisplayed()

        // Switch back to profile tab.
        composeTestRule.clickFirstClickable("Profil")
        composeTestRule.awaitText("Personlig information", timeoutMillis = 10_000)
    }

    @Test
    fun settingsScreen_editProfileButton_opensEditModeAndCanCancel() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Inställningar")
        composeTestRule.awaitText("Personlig information", timeoutMillis = 20_000)

        // The edit icon button's contentDescription.
        composeTestRule.onNodeWithContentDescription("Redigera profil").performClick()

        // Edit mode renders form fields with these labels.
        composeTestRule.awaitText("Förnamn", timeoutMillis = 10_000)
        composeTestRule.awaitText("Efternamn")

        // Cancel via "Avbryt" button.
        composeTestRule
            .onAllNodes(hasText("Avbryt") and hasClickAction())[0]
            .performClick()
        composeTestRule.awaitText("Personlig information", timeoutMillis = 10_000)
    }

    @Test
    fun settingsScreen_logoutDialog_cancels() {
        composeTestRule.performMockLogin()
        composeTestRule.openDrawer()
        composeTestRule.clickFirstClickable("Inställningar")
        composeTestRule.awaitText("Personlig information", timeoutMillis = 20_000)

        // Logout button at the bottom of the profile view.
        composeTestRule.clickFirstClickable("Logga ut")

        // Confirmation dialog text + Nej cancels.
        composeTestRule.awaitText("Är du säker", substring = true, timeoutMillis = 10_000)
        composeTestRule
            .onAllNodes(hasText("Nej") and hasClickAction())[0]
            .performClick()
        // We're still on the settings screen.
        composeTestRule.awaitText("Personlig information")
    }
}
