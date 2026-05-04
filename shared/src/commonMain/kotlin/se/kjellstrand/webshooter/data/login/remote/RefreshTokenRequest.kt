package se.kjellstrand.webshooter.data.login.remote

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val client_id: Int = 1,
    val client_secret: String,
    val grant_type: String = "refresh_token",
    val refresh_token: String
)
