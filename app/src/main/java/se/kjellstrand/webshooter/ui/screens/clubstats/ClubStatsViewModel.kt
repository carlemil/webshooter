package se.kjellstrand.webshooter.ui.screens.clubstats

import kotlinx.coroutines.flow.StateFlow

interface ClubStatsViewModel {
    val uiState: StateFlow<ClubStatsUiState>
}
