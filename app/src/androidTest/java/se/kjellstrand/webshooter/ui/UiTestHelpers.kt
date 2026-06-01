package se.kjellstrand.webshooter.ui

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.koin.mp.KoinPlatform
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.MockModeManager
import se.kjellstrand.webshooter.data.db.AppDatabase
import se.kjellstrand.webshooter.data.secure.SecurePrefs

/**
 * Wipes auth + DB + mock-mode flags so every test starts on a cold splash.
 */
fun resetAppStateForTests() {
    val koin = KoinPlatform.getKoin()
    MockModeManager.isMockMode = false
    koin.get<AuthTokenManager>().clearToken()
    koin.get<SecurePrefs>().clearUsername()
    koin.get<SecurePrefs>().clearMockMode()
    koin.get<AppDatabase>().clearAllTables()
}

fun ComposeTestRule.awaitText(
    text: String,
    substring: Boolean = false,
    timeoutMillis: Long = 15_000,
) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodesWithText(text, substring = substring)
            .fetchSemanticsNodes().isNotEmpty()
    }
}

fun ComposeTestRule.awaitContentDescription(
    description: String,
    timeoutMillis: Long = 15_000,
) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodes(hasContentDescription(description))
            .fetchSemanticsNodes().isNotEmpty()
    }
}

/**
 * Mockuser/mockpassword login. Waits until the competitions list has rendered.
 */
fun ComposeTestRule.performMockLogin() {
    awaitText("Användarnamn")
    onNodeWithText("Användarnamn").performTextInput("mockuser")
    onNodeWithText("Lösenord").performTextInput("mockpassword")
    // The screen title and the button both say "Inloggning"; pick the clickable.
    onNode(hasText("Inloggning") and hasClickAction()).performClick()

    // Competitions TopBar title is "Tävlingar".
    awaitText("Tävlingar", timeoutMillis = 20_000)
}

fun ComposeTestRule.openDrawer() {
    onNodeWithContentDescription("Meny").performClick()
}

/**
 * Click the first clickable node matching [text]. Disambiguates between e.g.
 * a drawer section header (label only) and the drawer menu item with the
 * same label (label + onClick).
 */
fun ComposeTestRule.clickFirstClickable(text: String, timeoutMillis: Long = 10_000) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodes(hasText(text) and hasClickAction())
            .fetchSemanticsNodes().isNotEmpty()
    }
    onAllNodes(hasText(text) and hasClickAction())[0].performClick()
}
