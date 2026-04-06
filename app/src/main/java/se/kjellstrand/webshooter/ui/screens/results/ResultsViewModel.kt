package se.kjellstrand.webshooter.ui.screens.results

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ResultsViewModel {
    val uiState: StateFlow<ResultsUiState>
    val competitionId: Long
    val competitionDate: String
    val resultsEvent: Flow<ResultsEvent>
    fun setMode(mode: Mode)
    fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>)
    fun setGroupingMode(groupingMode: GroupingMode)
    fun refresh()
}