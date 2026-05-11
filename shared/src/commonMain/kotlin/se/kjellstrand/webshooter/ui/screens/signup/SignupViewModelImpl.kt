package se.kjellstrand.webshooter.ui.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import se.kjellstrand.webshooter.data.signup.SignupRepository

class SignupViewModelImpl(
    override val competitionId: Long,
    private val signupRepository: SignupRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel(), SignupViewModel {

    private val _uiState = MutableStateFlow(SignupUiState())
    override val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private var loggedInUserId: Long = -1L

    init {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collect { resource ->
                if (resource is Resource.Success) {
                    loggedInUserId = resource.data.userId
                }
            }
        }
    }

    override fun selectWeaponClass(id: Long) {
        _uiState.update { it.copy(selectedWeaponClassId = id, error = null) }
    }

    override fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    override fun removeSignup(signupId: Long) {
        viewModelScope.launch {
            signupRepository.removeSignup(signupId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = resource.isLoading) }
                    is Resource.Success -> _uiState.update { it.copy(isSuccess = true, isLoading = false, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(error = resource.error::class.simpleName, isLoading = false) }
                }
            }
        }
    }

    override fun submit() {
        val weaponClassId = _uiState.value.selectedWeaponClassId ?: return
        viewModelScope.launch {
            signupRepository.signup(
                competitionId = competitionId,
                weaponClassId = weaponClassId,
                userId = loggedInUserId,
                note = _uiState.value.note
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = resource.isLoading) }
                    is Resource.Success -> _uiState.update { it.copy(isSuccess = true, isLoading = false, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(error = resource.error::class.simpleName, isLoading = false) }
                }
            }
        }
    }
}
