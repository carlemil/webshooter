package se.kjellstrand.webshooter.ui.screens.mysignups

import kotlinx.coroutines.flow.StateFlow

interface MySignupsViewModel {
    val uiState: StateFlow<MySignupsUiState>
    fun reload()
}
