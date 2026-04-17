package se.kjellstrand.webshooter.ui.screens.charts.clubstats

import se.kjellstrand.webshooter.data.clubstats.ShooterStats
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.WeaponClassGroup

data class ClubStatsUiState(
    val shooterStats: List<ShooterStats> = emptyList(),
    val year: Int = 0,
    val selectedGroup: WeaponClassGroup? = WeaponClassGroup.C,
    val availableGroups: Set<WeaponClassGroup> = setOf(
        WeaponClassGroup.A,
        WeaponClassGroup.B,
        WeaponClassGroup.C
    ),
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)
