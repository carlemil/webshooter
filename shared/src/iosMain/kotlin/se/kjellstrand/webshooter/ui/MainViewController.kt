package se.kjellstrand.webshooter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController
import se.kjellstrand.webshooter.ui.navigation.AppNavHost
import se.kjellstrand.webshooter.ui.theme.WebShooterTheme

private val napierInstalled: Boolean by lazy {
    Napier.base(DebugAntilog())
    true
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    @Suppress("UNUSED_EXPRESSION") napierInstalled
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
