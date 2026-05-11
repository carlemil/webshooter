package se.kjellstrand.webshooter.ui.screens.patrols

import kotlinx.coroutines.flow.StateFlow

interface PatrolsViewModel {
    val uiState: StateFlow<CompetitionPatrolsUiState>
}
