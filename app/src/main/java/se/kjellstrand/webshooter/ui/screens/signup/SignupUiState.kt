package se.kjellstrand.webshooter.ui.screens.signup

data class SignupUiState(
    val isLoading: Boolean = false,
    val selectedWeaponClassId: Long? = null,
    val note: String = "",
    val isSuccess: Boolean = false,
    val error: String? = null
)
