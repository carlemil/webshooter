package se.kjellstrand.webshooter.data.login.remote

import se.kjellstrand.webshooter.BuildConfig

data class RefreshTokenRequest(
    val client_id: Int = 1,
    val client_secret: String = BuildConfig.CLIENT_SECRET,
    val grant_type: String = "refresh_token",
    val refresh_token: String
)
