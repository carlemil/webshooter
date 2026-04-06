package se.kjellstrand.webshooter.ui.mock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.ui.screens.results.GroupingMode
import se.kjellstrand.webshooter.ui.screens.results.Mode
import se.kjellstrand.webshooter.ui.screens.results.ResultsEvent
import se.kjellstrand.webshooter.ui.screens.results.ResultsUiState
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModel
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModelImpl

class ResultsViewModelMock(
    initialState: ResultsUiState = ResultsUiState(
        MockResults().results,
        ResultsViewModelImpl.filterResults(MockResults().results, ResultsType.FIELD),
        ResultsViewModelImpl.groupResults(MockResults().results, ResultsType.FIELD),
        ResultsViewModelImpl.getWeaponGroups(MockResults().results).toList().sorted(),
        ResultsViewModelImpl.getWeaponGroups(MockResults().results),
        Mode.FILTER
    )
) : ViewModel(),
    ResultsViewModel {
    override val competitionId: Long = 0L
    override val competitionDate: String = ""
    override val uiState: StateFlow<ResultsUiState> = MutableStateFlow(initialState)
    override val resultsEvent: Flow<ResultsEvent> = emptyFlow()

    override fun setMode(mode: Mode) {}
    override fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>) {}
    override fun setGroupingMode(groupingMode: GroupingMode) {}
    override fun refresh() {}
}
