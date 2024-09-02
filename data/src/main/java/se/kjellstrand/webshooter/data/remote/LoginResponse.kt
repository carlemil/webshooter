package se.kjellstrand.webshooter.data.remote

data class LoginResponse(
    val token_type: String, //"Bearer",
    val expires_in: Int, //31536000,
    val access_token: String, //"mock-access-token",
    val refresh_token: String  // "mock-refresh-token"
)