package se.kjellstrand.webshooter.ui.mock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.results.Mode
import se.kjellstrand.webshooter.ui.results.ResultsUiState
import se.kjellstrand.webshooter.ui.results.ResultsViewModel
import se.kjellstrand.webshooter.ui.results.ResultsViewModelImpl

class ResultsViewModelMock() : ViewModel(),
    ResultsViewModel {
    override val uiState: StateFlow<ResultsUiState>
        get() {
            return MutableStateFlow(
                ResultsUiState(
                    MockResults().results,
                    ResultsViewModelImpl.filterResults(MockResults().results),
                    ResultsViewModelImpl.groupResults(MockResults().results),
                    ResultsViewModelImpl.getWeaponGroups(MockResults().results).toList().sorted(),
                    ResultsViewModelImpl.getWeaponGroups(MockResults().results),
                    Mode.FILTER
                )
            )
        }

    override fun setMode(mode: Mode) {
        TODO("Not yet implemented")
    }

    override fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>) {
        TODO("Not yet implemented")
    }
}
