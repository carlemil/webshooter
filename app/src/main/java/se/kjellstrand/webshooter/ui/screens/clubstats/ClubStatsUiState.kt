package se.kjellstrand.webshooter.ui.screens.clubstats

import se.kjellstrand.webshooter.data.clubstats.ShooterStats
import se.kjellstrand.webshooter.ui.screens.seriespoints.WeaponClassGroup

data class ClubStatsUiState(
    val shooterStats: List<ShooterStats> = emptyList(),
    val year: Int = 0,
    val selectedGroup: WeaponClassGroup? = null,
    val availableGroups: Set<WeaponClassGroup> = setOf(
        WeaponClassGroup.A,
        WeaponClassGroup.B,
        WeaponClassGroup.C
    ),
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)
