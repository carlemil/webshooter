package se.kjellstrand.webshooter.ui.screens.results

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ResultsViewModel {
    val uiState: StateFlow<ResultsUiState>
    val competitionId: Int
    val competitionDate: String
    val resultsEvent: SharedFlow<ResultsEvent>
    fun setMode(mode: Mode)
    fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>)
    fun setGroupingMode(groupingMode: GroupingMode)
    fun refresh()
}