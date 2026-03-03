package se.kjellstrand.webshooter.ui.screens.competitionpatrols

import kotlinx.coroutines.flow.StateFlow

interface CompetitionPatrolsViewModel {
    val uiState: StateFlow<CompetitionPatrolsUiState>
}
