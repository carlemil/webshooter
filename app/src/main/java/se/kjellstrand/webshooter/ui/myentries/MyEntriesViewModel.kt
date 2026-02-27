package se.kjellstrand.webshooter.ui.myentries

import kotlinx.coroutines.flow.StateFlow

interface MyEntriesViewModel {
    val uiState: StateFlow<MyEntriesUiState>
    fun reload()
}
