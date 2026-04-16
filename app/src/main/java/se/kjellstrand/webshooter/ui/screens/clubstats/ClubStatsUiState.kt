package se.kjellstrand.webshooter.ui.screens.clubstats

import se.kjellstrand.webshooter.data.clubstats.ShooterStats

data class ClubStatsUiState(
    val shooterStats: List<ShooterStats> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)
