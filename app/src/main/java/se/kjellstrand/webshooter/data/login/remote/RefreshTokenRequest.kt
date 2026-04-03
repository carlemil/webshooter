package se.kjellstrand.webshooter.data.login.remote

data class RefreshTokenRequest(
    val client_id: Int = 1,
    val client_secret: String = "52FphTYzOrmuqH30ltL7LrBzhSEURIJiMFNp6Qt0",
    val grant_type: String = "refresh_token",
    val refresh_token: String
)
