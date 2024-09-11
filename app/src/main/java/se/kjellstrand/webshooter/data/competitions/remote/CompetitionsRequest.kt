package se.kjellstrand.webshooter.data.competitions.remote

data class CompetitionsRequest(
    val client_id: Int = 1,
    val client_secret: String,
    val email: String,
    val grant_type: String = "password",
    val password: String,
    val username: String
)