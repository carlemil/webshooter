package se.kjellstrand.webshooter.ui.screens.myresults

import kotlinx.coroutines.flow.StateFlow

interface MyResultsViewModel {
    val uiState: StateFlow<MyResultsUiState>
    fun reload()
    fun onScreenOpened()
}
