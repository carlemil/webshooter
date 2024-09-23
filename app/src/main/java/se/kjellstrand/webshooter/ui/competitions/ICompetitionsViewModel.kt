package se.kjellstrand.webshooter.ui.competitions

import kotlinx.coroutines.flow.StateFlow

interface ICompetitionsViewModel {
    val uiState: StateFlow<CompetitionsUiState>
    // Add other properties or methods if needed
}