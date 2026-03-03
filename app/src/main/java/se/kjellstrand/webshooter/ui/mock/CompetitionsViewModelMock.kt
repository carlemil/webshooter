package se.kjellstrand.webshooter.ui.mock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsUiState
import se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModel

class CompetitionsViewModelMock() : ViewModel(),
    CompetitionsViewModel {
    override val uiState: StateFlow<CompetitionsUiState>
        get() {
            return MutableStateFlow(
                CompetitionsUiState(MockCompetitions().competitions)
            )
        }

    override fun getCompetitionById(competitionId: Long): Datum? {
        return MockCompetitions().competitions.data.find { it.id == competitionId }
    }

    override fun loadNextPage() {
        // Do nothing in mock.
    }

    override fun reload() {
        // Do nothing in mock.
    }

    override fun setSelectedCompetitionTypeIds(ids: Set<Int>) {
        // Do nothing in mock.
    }

    override fun setSelectedStatuses(statuses: Set<String>) {
        // Do nothing in mock.
    }
}
