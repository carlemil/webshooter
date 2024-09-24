package se.kjellstrand.webshooter.data.login.remote

import com.google.gson.annotations.SerializedName

data class LoginResponse (
    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)