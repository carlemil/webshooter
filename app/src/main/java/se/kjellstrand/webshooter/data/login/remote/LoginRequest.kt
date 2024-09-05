package se.kjellstrand.webshooter.data.login.remote

data class LoginRequest(
    val client_id: Int = 1,
    val client_secret: String,
    val email: String,
    val grant_type: String = "password",
    val password: String,
    val username: String
)