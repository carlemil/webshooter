package se.kjellstrand.webshooter.ui.results

import kotlinx.coroutines.flow.StateFlow

interface ResultsViewModel {
    val uiState: StateFlow<ResultsUiState>
    fun setMode(mode: Mode)
    fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>)
}