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
import se.kjellstrand.webshooter.data.login.LoginRepository
import se.kjellstrand.webshooter.ui.common.UiEvent
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        _uiState.value = LoginUiState()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginRepository.login(username, username, password)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            println("login Success: ${resource.data}")
                            resource.data.body()?.accessToken?.let { authTokenManager.storeToken(it) }
                            _uiState.value = LoginUiState(isSuccess = true)
                            _eventFlow.emit(UiEvent.NavigateToLandingPage)
                        }

                        is Resource.Error -> {
                            println("login Failed: ${resource.error}")
                            _uiState.value = LoginUiState(
                                isLoading = false,
                                errorMessage = resource.error.toString()
                            )
                            _eventFlow.emit(UiEvent.ShowErrorMessage(resource.error.toString()))
                        }

                        else -> {
                            println("login isLoading = true")
                            _uiState.value = LoginUiState(isLoading = true)
                        }
                    }
                }
        }
    }

    fun getCookies() {
        viewModelScope.launch {
            loginRepository.getCookies()
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            println("getCookies Success: ${resource.data}")
                        }

                        is Resource.Error -> {
                            println("getCookies Failed: ${resource.error}")
                            _eventFlow.emit(UiEvent.ShowErrorMessage(resource.error.toString()))
                        }

                        else -> {
                            println("getCookies isLoading = true")
                        }
                    }
                }
        }
    }
}
