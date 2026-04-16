package se.kjellstrand.webshooter.data.clubstats

data class ShooterStats(
    val userId: Long,
    val fullname: String,
    val averagePoints: Double,
    val competitionCount: Int
)
