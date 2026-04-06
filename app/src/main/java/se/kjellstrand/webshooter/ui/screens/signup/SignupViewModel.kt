package se.kjellstrand.webshooter.ui.screens.signup

import kotlinx.coroutines.flow.StateFlow

interface SignupViewModel {
    val competitionId: Long
    val uiState: StateFlow<SignupUiState>
    fun selectWeaponClass(id: Long)
    fun updateNote(note: String)
    fun removeSignup(signupId: Long)
    fun submit()
}
