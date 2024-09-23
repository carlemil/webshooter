package se.kjellstrand.webshooter.ui

import kotlinx.coroutines.flow.StateFlow

interface ICompetitionsViewModel {
    val uiState: StateFlow<CompetitionsUiState>
    // Add other properties or methods if needed
}