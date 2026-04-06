package se.kjellstrand.webshooter.ui.screens.club

import kotlinx.coroutines.flow.StateFlow

interface ClubViewModel {
    val uiState: StateFlow<ClubUiState>
    fun selectTab(tab: ClubTab)
}
