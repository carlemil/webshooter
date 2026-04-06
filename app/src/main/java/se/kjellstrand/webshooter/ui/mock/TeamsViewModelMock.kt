package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.teams.CompetitionTeamsUiState
import se.kjellstrand.webshooter.ui.screens.teams.TeamsViewModel

class TeamsViewModelMock(
    initialState: CompetitionTeamsUiState = CompetitionTeamsUiState(
        teams = MockTeams().teams
    )
) : TeamsViewModel {
    override val uiState: StateFlow<CompetitionTeamsUiState> = MutableStateFlow(initialState)
}
