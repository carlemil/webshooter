package se.kjellstrand.webshooter.ui.login

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
    val savedPassword = securePrefs.getPassword()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        attemptAutoLogin()
    }

    private fun attemptAutoLogin() {
        if (savedUsername.isNotEmpty() && savedPassword.isNotEmpty()) {
            login(savedUsername, savedPassword, isAutoLogin = true)
        } else {
            _uiState.value = _uiState.value.copy(autoLoginAttempted = true)
        }
    }

    fun login(username: String, password: String, isAutoLogin: Boolean = false) {
        viewModelScope.launch {
            loginRepository.login(username, username, password)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data.body()?.accessToken?.let { authTokenManager.storeToken(it) }
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSuccess = true,
                                autoLoginAttempted = true
                            )
                            _eventFlow.emit(UiEvent.NavigateToLandingPage)
                            securePrefs.saveCredentials(username, password)
                        }

                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = resource.error.toString(),
                                autoLoginAttempted = true
                            )
                            if (!isAutoLogin) {
                                _eventFlow.emit(UiEvent.ShowErrorMessage(resource.error.toString()))
                            }
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
                        is Resource.Success -> {
                            println("getCookies Success: ${resource.data}")
                        }

                        is Resource.Error -> {
                            println("getCookies Failed: ${resource.error}")
                            _eventFlow.emit(UiEvent.ShowErrorMessage(resource.error.toString()))
                        }

                        is Resource.Loading -> {
                            println("getCookies isLoading = true")
                        }
                    }
                }
        }
    }
}
