@file:Suppress("OPT_IN_USAGE")

package se.kjellstrand.webshooter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AccessibilitySyncOptions
import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.MockModeManager
import se.kjellstrand.webshooter.data.secure.SecurePrefs
import se.kjellstrand.webshooter.ui.navigation.AppNavHost
import se.kjellstrand.webshooter.ui.theme.WebShooterTheme

private val napierInstalled: Boolean by lazy {
    Napier.base(DebugAntilog())
    true
}

/**
 * If `AUTOLOGIN=mock` is in the launch environment, seed the auth manager
 * with the same fake-token state the LoginViewModel writes when the user
 * types mockuser/mockpassword. Lets XCUITest drive the real AppNavHost
 * without having to interact with the Compose login form — Compose iOS
 * TextFields don't accept synthetic XCUITest keystrokes.
 */
private fun applyMockAutoLoginIfRequested() {
    val env = NSProcessInfo.processInfo.environment["AUTOLOGIN"] as? String
    if (env != "mock") return
    val koin = KoinPlatform.getKoin()
    MockModeManager.isMockMode = true
    koin.get<SecurePrefs>().saveMockMode(true)
    koin.get<SecurePrefs>().saveUsername("mockuser")
    koin.get<AuthTokenManager>().storeTokens("mock_token", "mock_refresh_token", 3600)
}

fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        // Bridge the Compose semantics tree into UIAccessibility unconditionally.
        // The default (WhenRequiredByAccessibilityServices) only enables the
        // bridge when VoiceOver/Switch Control is active, which leaves XCUITest
        // unable to find any Compose text. Always-on costs little for end users
        // and is what makes the XCUITest suite in iosAppUITests/ functional.
        accessibilitySyncOptions = AccessibilitySyncOptions.Always(debugLogger = null)
    }
) {
    @Suppress("UNUSED_EXPRESSION") napierInstalled
    applyMockAutoLoginIfRequested()
    WebShooterTheme {
        // If the launcher sets the SCREEN env var, render the corresponding
        // mock-driven screen in DebugGallery instead of the real AppNavHost.
        // Lets us walk every Compose screen on Simulator without auth.
        val screenName = NSProcessInfo.processInfo.environment["SCREEN"] as? String
        if (screenName != null) {
            DebugGallery(screenName)
        } else {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavHost(
                    navController = navController,
                    showMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
