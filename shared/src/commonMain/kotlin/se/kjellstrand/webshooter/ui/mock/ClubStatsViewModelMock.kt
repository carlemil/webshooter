package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.clubstats.ShooterStats
import se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup
import se.kjellstrand.webshooter.ui.screens.charts.clubstats.ClubStatsUiState
import se.kjellstrand.webshooter.ui.screens.charts.clubstats.ClubStatsViewModel

class ClubStatsViewModelMock(
    initialState: ClubStatsUiState = defaultState(),
) : ClubStatsViewModel {
    override val uiState: StateFlow<ClubStatsUiState> = MutableStateFlow(initialState)
    override fun selectWeaponGroup(group: WeaponClassGroup?) {}
    override fun selectYear(year: Int) {}
    override fun refresh() {}
}

private fun defaultState(): ClubStatsUiState = ClubStatsUiState(
    shooterStats = listOf(
        ShooterStats(userId = 101L, fullname = "Erik Svensson", averagePoints = 47.2, competitionCount = 12),
        ShooterStats(userId = 102L, fullname = "Anna Lindgren", averagePoints = 45.8, competitionCount = 9),
        ShooterStats(userId = 103L, fullname = "Oskar Berg", averagePoints = 44.5, competitionCount = 14),
        ShooterStats(userId = 104L, fullname = "Karin Holm", averagePoints = 43.9, competitionCount = 7),
        ShooterStats(userId = 105L, fullname = "Lars Karlsson", averagePoints = 42.1, competitionCount = 11),
        ShooterStats(userId = 106L, fullname = "Maria Ek", averagePoints = 41.7, competitionCount = 5),
    ),
    year = 2026,
    availableYears = listOf(2026, 2025, 2024),
    selectedGroup = WeaponClassGroup.C,
)
