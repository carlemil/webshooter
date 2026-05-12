package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.patrols.CompetitionPatrolsUiState
import se.kjellstrand.webshooter.ui.screens.patrols.PatrolsViewModel

class PatrolsViewModelMock(
    initialState: CompetitionPatrolsUiState = CompetitionPatrolsUiState(
        patrols = MockPatrols().patrols
    )
) : PatrolsViewModel {
    override val uiState: StateFlow<CompetitionPatrolsUiState> = MutableStateFlow(initialState)
}
