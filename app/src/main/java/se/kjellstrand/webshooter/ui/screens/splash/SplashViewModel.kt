package se.kjellstrand.webshooter.ui.screens.splash

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.collect
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.cookies.CookiesRepository
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authTokenManager: AuthTokenManager,
    private val cookiesRepository: CookiesRepository
) : ViewModel() {
    fun hasSession(): Boolean =
        authTokenManager.readToken() != null || authTokenManager.readRefreshToken() != null

    /**
     * Hit "/" so the in-memory cookie jar (AcceptAllCookiesStorage) gets the
     * Laravel session cookies. Bearer-token endpoints 500 without them.
     * Called before the splash navigates to LandingScreen on a warm-start
     * session — the LoginScreen path already does this in its LaunchedEffect.
     */
    suspend fun primeCookies() {
        try {
            cookiesRepository.getCookies().collect { /* drain */ }
        } catch (e: Exception) {
            Napier.w("primeCookies failed", e, "SplashViewModel")
        }
    }
}
