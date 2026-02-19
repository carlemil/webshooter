package se.kjellstrand.webshooter.ui.settings

import se.kjellstrand.webshooter.data.settings.remote.UserProfile

data class SettingsUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val isEditMode: Boolean = false,
    val editName: String = "",
    val editLastname: String = "",
    val editEmail: String = "",
    val editMobile: String = "",
    val editPhone: String = "",
    val editGender: String = "",
    val editBirthday: Int? = null,
    val editShootingCardNumber: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val selectedTab: SettingsTab = SettingsTab.PROFILE
)

enum class SettingsTab { PROFILE, PASSWORD }
