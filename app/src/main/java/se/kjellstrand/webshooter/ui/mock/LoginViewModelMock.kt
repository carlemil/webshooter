package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.common.UiEvent
import se.kjellstrand.webshooter.ui.screens.login.LoginUiState
import se.kjellstrand.webshooter.ui.screens.login.LoginViewModel

class LoginViewModelMock(
    initialState: LoginUiState = LoginUiState(autoLoginAttempted = true)
) : LoginViewModel {
    override val savedUsername: String = ""
    override val uiState: StateFlow<LoginUiState> = MutableStateFlow(initialState)
    override val eventFlow: SharedFlow<UiEvent> = MutableSharedFlow()
    override fun login(username: String, password: String) {}
    override fun getCookies() {}
}
