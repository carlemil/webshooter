package se.kjellstrand.webshooter.ui.screens.settings

import kotlinx.coroutines.flow.StateFlow

interface SettingsViewModel {
    val uiState: StateFlow<SettingsUiState>
    fun loadProfile()
    fun setTab(tab: SettingsTab)
    fun setEditMode(enabled: Boolean)
    fun onNameChange(value: String)
    fun onLastnameChange(value: String)
    fun onEmailChange(value: String)
    fun onMobileChange(value: String)
    fun onPhoneChange(value: String)
    fun onGenderChange(gender: Gender)
    fun onBirthdayChange(value: Int)
    fun onShootingCardNumberChange(value: String)
    fun onCurrentPasswordChange(value: String)
    fun onNewPasswordChange(value: String)
    fun onConfirmPasswordChange(value: String)
    fun saveProfile()
    fun updatePassword()
    fun logout()
    fun clearMessages()
}
