package se.kjellstrand.webshooter.ui.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Cross-platform inbox for inbound deep-link URLs. Hosts dispatch raw URL
 * strings here from their platform-specific entry points (e.g. iOS
 * SwiftUI `.onOpenURL` / `.onContinueUserActivity`), and the Compose-side
 * `AppNavHost` collects + routes them via [DeepLinkRouter].
 *
 * `replay = 1` so a URL arriving before `AppNavHost` is composed (e.g.
 * cold-start launch via tapping a Universal Link) is still picked up.
 */
object DeepLinkBus {
    private val _incoming = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 4)
    val incoming: SharedFlow<String> = _incoming.asSharedFlow()

    /** Called by platform hosts. Safe to call from any thread. */
    fun dispatch(rawUrl: String) {
        _incoming.tryEmit(rawUrl)
    }
}
