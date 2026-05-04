package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.settings.Gender
import se.kjellstrand.webshooter.ui.screens.settings.SettingsUiState
import se.kjellstrand.webshooter.ui.screens.settings.SettingsTab
import se.kjellstrand.webshooter.ui.screens.settings.SettingsViewModel

class SettingsViewModelMock(
    initialState: SettingsUiState = SettingsUiState(profile = MockSettings().userProfile)
) : SettingsViewModel {
    override val uiState: StateFlow<SettingsUiState> = MutableStateFlow(initialState)
    override fun loadProfile() {}
    override fun setTab(tab: SettingsTab) {}
    override fun setEditMode(enabled: Boolean) {}
    override fun onNameChange(value: String) {}
    override fun onLastnameChange(value: String) {}
    override fun onEmailChange(value: String) {}
    override fun onMobileChange(value: String) {}
    override fun onPhoneChange(value: String) {}
    override fun onGenderChange(gender: Gender) {}
    override fun onBirthdayChange(value: Int) {}
    override fun onShootingCardNumberChange(value: String) {}
    override fun onCurrentPasswordChange(value: String) {}
    override fun onNewPasswordChange(value: String) {}
    override fun onConfirmPasswordChange(value: String) {}
    override fun saveProfile() {}
    override fun updatePassword() {}
    override fun logout() {}
    override fun clearMessages() {}
}
