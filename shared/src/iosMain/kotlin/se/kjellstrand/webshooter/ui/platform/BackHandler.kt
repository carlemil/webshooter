package se.kjellstrand.webshooter.ui.platform

import androidx.compose.runtime.Composable

/**
 * iOS actual: intentionally no-op.
 *
 * Compose Multiplatform 1.7.x does not wire any back-event source through
 * `ComposeUIViewController`. There is no JetBrains port of
 * `androidx.activity.compose.BackHandler` for iOS in this CMP/Kotlin stack
 * (the JB `navigationevent-compose` artifact requires CMP 1.8+).
 *
 * Net effect: any `BackHandler { ... }` block in commonMain becomes a no-op
 * on iOS. The single in-repo caller (`WebShooterScreen` drawer-route → back
 * → CompetitionsList) is reached by iOS users via the drawer menu, not by a
 * back signal. When CMP is bumped to 1.8+, replace this body with a delegate
 * to `androidx.activity.compose.BackHandler` (or `navigationevent-compose`).
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No iOS back-event source available with this CMP/dep stack.
}
