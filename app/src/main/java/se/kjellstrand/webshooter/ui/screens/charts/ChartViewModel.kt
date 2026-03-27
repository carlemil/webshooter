package se.kjellstrand.webshooter.ui.screens.charts

import kotlinx.coroutines.flow.StateFlow

interface ChartViewModel {
    val uiState: StateFlow<ChartUiState>
}
