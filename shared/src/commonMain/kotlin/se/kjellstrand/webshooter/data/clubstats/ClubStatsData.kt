package se.kjellstrand.webshooter.data.clubstats

data class ClubStatsData(
    val shooterStats: List<ShooterStats>,
    val availableYears: List<Int> = emptyList()
)
