package se.kjellstrand.webshooter.ui.screens.splash

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import se.kjellstrand.webshooter.data.AuthTokenManager
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authTokenManager: AuthTokenManager
) : ViewModel() {
    fun hasSession(): Boolean =
        authTokenManager.readToken() != null || authTokenManager.readRefreshToken() != null
}
