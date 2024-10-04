package se.kjellstrand.webshooter.ui.results

import kotlinx.coroutines.flow.StateFlow

interface ResultsViewModel {
    val uiState: StateFlow<ResultsUiState>
    fun setLayoutType(mode: Mode)
}