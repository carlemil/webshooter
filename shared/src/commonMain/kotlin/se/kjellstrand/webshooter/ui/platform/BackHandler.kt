package se.kjellstrand.webshooter.ui.platform

import androidx.compose.runtime.Composable

/**
 * Multiplatform stand-in for `androidx.activity.compose.BackHandler` —
 * intercepts the platform's "back" action when [enabled] is true.
 *
 * Android: delegates to androidx.activity.compose.BackHandler (handles the
 * system back gesture / hardware back button via OnBackPressedDispatcher).
 *
 * iOS: no-op for now. iOS doesn't have a system back button; the back-swipe
 * gesture is owned by UINavigationController, which the Compose host does
 * not embed inside. If we later need to intercept it, this becomes a real
 * implementation using UIScreenEdgePanGestureRecognizer or a Compose-side
 * predictive-back overlay.
 */
@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)
