package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.signup.SignupUiState
import se.kjellstrand.webshooter.ui.screens.signup.SignupViewModel

class SignupViewModelMock(
    initialState: SignupUiState = SignupUiState()
) : SignupViewModel {
    override val competitionId: Long = 196L
    override val uiState: StateFlow<SignupUiState> = MutableStateFlow(initialState)
    override fun selectWeaponClass(id: Long) {}
    override fun updateNote(note: String) {}
    override fun removeSignup(signupId: Long) {}
    override fun submit() {}
}
