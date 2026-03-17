package se.kjellstrand.webshooter.ui.screens.patrols

import kotlinx.coroutines.flow.StateFlow

interface CompetitionPatrolsViewModel {
    val uiState: StateFlow<CompetitionPatrolsUiState>
}
