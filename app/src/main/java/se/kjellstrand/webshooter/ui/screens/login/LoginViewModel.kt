package se.kjellstrand.webshooter.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.AuthTokenManager
import se.kjellstrand.webshooter.data.MockModeManager
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.cookies.CookiesRepository
import se.kjellstrand.webshooter.data.login.LoginRepository
import se.kjellstrand.webshooter.data.secure.SecurePrefs
import se.kjellstrand.webshooter.ui.common.UiEvent
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val cookiesRepository: CookiesRepository,
    private val authTokenManager: AuthTokenManager,
    internal val securePrefs: SecurePrefs
) : ViewModel() {

    val savedUsername = securePrefs.getUsername()

    private val _uiState = MutableStateFlow(
        LoginUiState(autoLoginAttempted = true)
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            if (username == "mockuser" && password == "mockpassword") {
                MockModeManager.isMockMode = true
                authTokenManager.storeTokens("mock_token", "mock_refresh_token", 3600)
                securePrefs.saveUsername(username)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                _eventFlow.emit(UiEvent.NavigateToLandingPage)
                return@launch
            }

            loginRepository.login(username, username, password)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data.body()?.let { loginResponse ->
                                authTokenManager.storeTokens(
                                    loginResponse.accessToken,
                                    loginResponse.refreshToken,
                                    loginResponse.expiresIn
                                )
                            }
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSuccess = true
                            )
                            _eventFlow.emit(UiEvent.NavigateToLandingPage)
                            securePrefs.saveUsername(username)
                        }

                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = resource.error.toString()
                            )
                            _eventFlow.emit(UiEvent.ShowErrorMessage(resource.error.toString()))
                        }

                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
        }
    }

    fun getCookies() {
        viewModelScope.launch {
            cookiesRepository.getCookies()
                .collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            _eventFlow.emit(UiEvent.ShowErrorMessage(resource.error.toString()))
                        }

                        else -> {}
                    }
                }
        }
    }
}
