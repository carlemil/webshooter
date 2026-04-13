package se.kjellstrand.webshooter.ui.mock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsUiState
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModel

class CompetitionsViewModelMock(
    initialState: CompetitionsUiState = CompetitionsUiState(MockCompetitions().competitions)
) : ViewModel(),
    CompetitionsViewModel {
    override val uiState: StateFlow<CompetitionsUiState> = MutableStateFlow(initialState)

    override fun getCompetitionById(competitionId: Long): Datum? {
        return uiState.value.competitions?.data?.find { it.id == competitionId }
    }

    override fun reload() {}
    override fun setSelectedCompetitionTypeIds(ids: Set<Int>) {}
    override fun setSelectedStatuses(statuses: Set<String>) {}
}
