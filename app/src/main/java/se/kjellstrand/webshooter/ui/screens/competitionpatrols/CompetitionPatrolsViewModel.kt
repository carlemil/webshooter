package se.kjellstrand.webshooter.ui.competitionpatrols

import kotlinx.coroutines.flow.StateFlow

interface CompetitionPatrolsViewModel {
    val uiState: StateFlow<CompetitionPatrolsUiState>
}
