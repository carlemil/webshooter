package se.kjellstrand.webshooter.data.results.local

data class ClubStatsRow(
    val userId: Long,
    val fullname: String,
    val averagePoints: Double,
    val competitionCount: Int
)
