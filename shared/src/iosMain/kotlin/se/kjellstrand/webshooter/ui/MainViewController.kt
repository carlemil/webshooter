package se.kjellstrand.webshooter.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import se.kjellstrand.webshooter.ui.screens.login.LoginScreen
import se.kjellstrand.webshooter.ui.theme.WebShooterTheme

fun MainViewController(): UIViewController = ComposeUIViewController {
    WebShooterTheme {
        LoginScreen(onLoginSuccess = { /* Phase 4 wires up real navigation */ })
    }
}
