package se.kjellstrand.webshooter.data.remote

data class UserRequest(
    val pistolShooterCardNumber: Int,
    val firstName: String?,
    val lastName: String?
)