package se.kjellstrand.webshooter.data.login

class Login(
    val tokenType: String,
    val expiresIn: Int,
    val accessToken: String,
    val refreshToken: String
)
