package se.kjellstrand.webshooter.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import se.kjellstrand.webshooter.data.settings.remote.UserProfile
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = resource.isLoading)
                    is Resource.Success -> {
                        val profile = resource.data
                        _uiState.value = _uiState.value.copy(
                            profile = profile,
                            editName = profile.name,
                            editLastname = profile.lastname,
                            editEmail = profile.email,
                            editMobile = profile.mobile ?: "",
                            editPhone = profile.phone ?: "",
                            editGender = profile.gender ?: "",
                            editBirthday = profile.birthday?.substringBefore("-")?.toIntOrNull(),
                            editShootingCardNumber = profile.shootingCardNumber ?: ""
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(errorMessage = resource.error.name)
                    }
                }
            }
        }
    }

    fun setTab(tab: SettingsTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, successMessage = null, errorMessage = null)
    }

    fun setEditMode(enabled: Boolean) {
        val current = _uiState.value
        _uiState.value = if (enabled) {
            current.copy(
                isEditMode = true,
                editName = current.profile?.name ?: "",
                editLastname = current.profile?.lastname ?: "",
                editEmail = current.profile?.email ?: "",
                editMobile = current.profile?.mobile ?: "",
                editPhone = current.profile?.phone ?: "",
                editGender = current.profile?.gender ?: "",
                editBirthday = current.profile?.birthday?.substringBefore("-")?.toIntOrNull(),
                editShootingCardNumber = current.profile?.shootingCardNumber ?: "",
                successMessage = null,
                errorMessage = null
            )
        } else {
            current.copy(isEditMode = false, successMessage = null, errorMessage = null)
        }
    }

    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(editName = value) }
    fun onLastnameChange(value: String) { _uiState.value = _uiState.value.copy(editLastname = value) }
    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(editEmail = value) }
    fun onMobileChange(value: String) { _uiState.value = _uiState.value.copy(editMobile = value) }
    fun onPhoneChange(value: String) { _uiState.value = _uiState.value.copy(editPhone = value) }
    fun onGenderChange(value: String) { _uiState.value = _uiState.value.copy(editGender = value) }
    fun onBirthdayChange(value: Int) { _uiState.value = _uiState.value.copy(editBirthday = value) }
    fun onShootingCardNumberChange(value: String) { _uiState.value = _uiState.value.copy(editShootingCardNumber = value) }

    fun onCurrentPasswordChange(value: String) { _uiState.value = _uiState.value.copy(currentPassword = value) }
    fun onNewPasswordChange(value: String) { _uiState.value = _uiState.value.copy(newPassword = value) }
    fun onConfirmPasswordChange(value: String) { _uiState.value = _uiState.value.copy(confirmPassword = value) }

    fun saveProfile() {
        val state = _uiState.value
        val existingProfile = state.profile ?: return
        val updatedProfile = UserProfile(
            name = state.editName,
            lastname = state.editLastname,
            email = state.editEmail,
            shootingCardNumber = state.editShootingCardNumber.ifEmpty { null },
            noShootingCardNumber = existingProfile.noShootingCardNumber,
            birthday = state.editBirthday?.let { "$it-01-01" },
            gender = state.editGender.ifEmpty { null },
            phone = state.editPhone.ifEmpty { null },
            mobile = state.editMobile.ifEmpty { null },
            gradeField = existingProfile.gradeField,
            gradeTrackshooting = existingProfile.gradeTrackshooting,
            apiToken = existingProfile.apiToken,
            userId = existingProfile.userId,
            fullname = "${state.editName} ${state.editLastname}",
            clubsId = existingProfile.clubsId,
            status = existingProfile.status,
            clubs = existingProfile.clubs
        )
        viewModelScope.launch {
            settingsRepository.updateUserProfile(updatedProfile).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = resource.isLoading)
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            profile = resource.data,
                            isEditMode = false,
                            successMessage = "Profile updated successfully"
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to save profile")
                    }
                }
            }
        }
    }

    fun updatePassword() {
        val state = _uiState.value
        if (state.newPassword != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            settingsRepository.updatePassword(
                state.currentPassword,
                state.newPassword,
                state.confirmPassword
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = resource.isLoading)
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            currentPassword = "",
                            newPassword = "",
                            confirmPassword = "",
                            successMessage = "Password updated successfully"
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(errorMessage = "Failed to update password")
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
