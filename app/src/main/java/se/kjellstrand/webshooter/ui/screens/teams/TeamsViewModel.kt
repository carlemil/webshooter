package se.kjellstrand.webshooter.ui.screens.teams

import kotlinx.coroutines.flow.StateFlow

interface TeamsViewModel {
    val uiState: StateFlow<CompetitionTeamsUiState>
}
