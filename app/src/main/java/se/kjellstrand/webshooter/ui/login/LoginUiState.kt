package se.kjellstrand.webshooter.ui.login

import se.kjellstrand.webshooter.data.login.remote.LoginResponse

data class LoginUiState(
    val loginResponse: LoginResponse? = null
)