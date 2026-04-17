package se.kjellstrand.webshooter.ui.screens.clubstats

import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.seriespoints.WeaponClassGroup

interface ClubStatsViewModel {
    val uiState: StateFlow<ClubStatsUiState>
    fun selectWeaponGroup(group: WeaponClassGroup?)
}
