package se.kjellstrand.webshooter.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.Resource
import se.kjellstrand.webshooter.data.login.LoginRepository
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) :  ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = LoginUiState()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginRepository.login(username,username,password).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        println("getCompetitions Success: ${resource.data}")
                        _uiState.value = LoginUiState(resource.data)
                    }

                    is Resource.Error -> {
                        println("getCompetitions Failed: ${resource.error}")
                    }

                    else -> {
                        println("getCompetitions other state")
                    }
                }
            }
        }
    }
}
