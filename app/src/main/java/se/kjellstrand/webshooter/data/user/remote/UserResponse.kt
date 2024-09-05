package se.kjellstrand.webshooter.data.user.remote

data class UserResponse(
    val pistolShooterCardNumber: Int,
    val firstName: String?,
    val lastName: String?
)