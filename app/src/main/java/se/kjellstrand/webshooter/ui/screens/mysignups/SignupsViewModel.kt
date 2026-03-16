package se.kjellstrand.webshooter.ui.screens.mysignups

import kotlinx.coroutines.flow.StateFlow

interface SignupsViewModel {
    val uiState: StateFlow<SignupsUiState>
    fun reload()
}
