package se.kjellstrand.webshooter.ui.mock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.ui.screens.results.GroupingMode
import se.kjellstrand.webshooter.ui.screens.results.Mode
import se.kjellstrand.webshooter.ui.screens.results.ResultsEvent
import se.kjellstrand.webshooter.ui.screens.results.ResultsUiState
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModel
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModelImpl

class ResultsViewModelMock() : ViewModel(),
    ResultsViewModel {
    override val competitionId: Long = 0
    override val competitionDate: String = ""
    override val uiState: StateFlow<ResultsUiState>
        get() {
            return MutableStateFlow(
                ResultsUiState(
                    MockResults().results,
                    ResultsViewModelImpl.filterResults(MockResults().results, ResultsType.FIELD),
                    ResultsViewModelImpl.groupResults(MockResults().results, ResultsType.FIELD),
                    ResultsViewModelImpl.getWeaponGroups(MockResults().results).toList().sorted(),
                    ResultsViewModelImpl.getWeaponGroups(MockResults().results),
                    Mode.FILTER
                )
            )
        }
    override val resultsEvent: SharedFlow<ResultsEvent> = MutableSharedFlow()

    override fun setMode(mode: Mode) {}

    override fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>) {}

    override fun setGroupingMode(groupingMode: GroupingMode) {}

    override fun refresh() {}
}
