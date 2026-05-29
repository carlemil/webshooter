package se.kjellstrand.webshooter.ui

import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import io.github.aakira.napier.Napier
import platform.UIKit.UIViewController
import se.kjellstrand.webshooter.ui.navigation.AppNavHost
import se.kjellstrand.webshooter.ui.theme.WebShooterTheme

fun MainViewController(): UIViewController = ComposeUIViewController {
    WebShooterTheme {
        val navController = rememberNavController()
        AppNavHost(
            navController = navController,
            // Phase 5 can upgrade this to a SnackbarHostState or UIAlert.
            // For now the messages just go to the iOS log.
            showMessage = { Napier.i(it, tag = "Webshooter") },
        )
    }
}
