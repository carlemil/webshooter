package se.kjellstrand.webshooter.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import se.kjellstrand.webshooter.ui.screens.licenses.LicensesScreen
import se.kjellstrand.webshooter.ui.theme.WebShooterTheme

fun MainViewController(): UIViewController = ComposeUIViewController {
    WebShooterTheme {
        LicensesScreen()
    }
}
