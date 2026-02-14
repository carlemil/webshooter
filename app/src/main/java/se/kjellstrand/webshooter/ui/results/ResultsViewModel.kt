package se.kjellstrand.webshooter.ui.results

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ResultsViewModel {
    val uiState: StateFlow<ResultsUiState>
    val competitionId: Int
    val resultsEvent: SharedFlow<ResultsEvent>
    fun setMode(mode: Mode)
    fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>)
}