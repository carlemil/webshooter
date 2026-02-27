package se.kjellstrand.webshooter.ui.myentries

import se.kjellstrand.webshooter.data.competitions.remote.Datum

data class MyEntriesUiState(
    val entries: List<Datum> = emptyList(),
    val isLoading: Boolean = false,
    val isFinished: Boolean = false
)
