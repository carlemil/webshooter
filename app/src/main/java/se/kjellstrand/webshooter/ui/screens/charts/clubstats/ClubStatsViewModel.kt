package se.kjellstrand.webshooter.ui.screens.charts.clubstats

import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.WeaponClassGroup

interface ClubStatsViewModel {
    val uiState: StateFlow<ClubStatsUiState>
    fun selectWeaponGroup(group: WeaponClassGroup?)
    fun selectYear(year: Int)
}
