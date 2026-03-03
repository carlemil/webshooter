package se.kjellstrand.webshooter.ui.signups

import kotlinx.coroutines.flow.StateFlow

interface SignupsViewModel {
    val uiState: StateFlow<SignupsUiState>
    fun reload()
}
