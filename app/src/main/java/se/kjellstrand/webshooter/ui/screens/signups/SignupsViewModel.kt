package se.kjellstrand.webshooter.ui.screens.signups

import kotlinx.coroutines.flow.StateFlow

interface SignupsViewModel {
    val uiState: StateFlow<SignupsUiState>
    fun reload()
}
