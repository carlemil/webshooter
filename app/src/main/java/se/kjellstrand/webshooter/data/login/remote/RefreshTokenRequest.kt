package se.kjellstrand.webshooter.data.login.remote

import se.kjellstrand.webshooter.data.login.LoginRepository

data class RefreshTokenRequest(
    val client_id: Int = 1,
    val client_secret: String = LoginRepository.CLIENT_SECRET,
    val grant_type: String = "refresh_token",
    val refresh_token: String
)
