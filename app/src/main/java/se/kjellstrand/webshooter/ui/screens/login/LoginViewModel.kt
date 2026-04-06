package se.kjellstrand.webshooter.ui.screens.login

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.common.UiEvent

interface LoginViewModel {
    val savedUsername: String
    val uiState: StateFlow<LoginUiState>
    val eventFlow: SharedFlow<UiEvent>
    fun login(username: String, password: String)
    fun getCookies()
}
