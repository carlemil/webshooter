package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.myresults.MyResultsUiState
import se.kjellstrand.webshooter.ui.screens.myresults.MyResultsViewModel

class MyResultsViewModelMock(
    initialState: MyResultsUiState = MyResultsUiState(
        groupedEntries = MockMyResults().groupedEntries,
        resultStats = MockMyResults().resultStats
    )
) : MyResultsViewModel {
    override val uiState: StateFlow<MyResultsUiState> = MutableStateFlow(initialState)
    override fun reload() {}
}
