package se.kjellstrand.webshooter.data.login.remote

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class LoginResponse (
    @Json(name = "token_type")
    val tokenType: String,

    @Json(name = "expires_in")
    val expiresIn: Long,

    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "refresh_token")
    val refreshToken: String
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<LoginResponse>(json)
    }
}