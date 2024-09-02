package se.kjellstrand.webshooter.data.remote

data class LoginRequest(
    val client_id: Int,
    val client_secret: String,
    val email: String = "erbsman@gmail.com",
    val grant_type: String = "password",
    val password: String = "REMOVED-OLD-PASSWORD",
    val username: String = "erbsman@gmail.com"
)