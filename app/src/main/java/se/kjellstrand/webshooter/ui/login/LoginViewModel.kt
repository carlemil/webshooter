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
import se.kjellstrand.webshooter.data.Resource
import se.kjellstrand.webshooter.data.login.LoginRepository
import se.kjellstrand.webshooter.ui.UiEvent
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) :  ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        _uiState.value = LoginUiState()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginRepository.login(username,username,password).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        println("getCompetitions Success: ${resource.data}")
                        _uiState.value = LoginUiState(isSuccess = true)
                        _eventFlow.emit(UiEvent.NavigateToLandingPage)
                    }

                    is Resource.Error -> {
                        println("getCompetitions Failed: ${resource.error}")
                        _uiState.value = LoginUiState(
                            isLoading = false,
                            errorMessage = resource.error.toString()
                        )
                        _eventFlow.emit(UiEvent.ShowErrorMessage(resource.error.toString()))
                    }

                    else -> {
                        println("getCompetitions other state")
                        _uiState.value = LoginUiState(isLoading = true)
                    }
                }
            }
        }
    }
}
