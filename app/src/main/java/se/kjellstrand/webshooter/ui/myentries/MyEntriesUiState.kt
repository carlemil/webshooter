package se.kjellstrand.webshooter.ui.myentries

import se.kjellstrand.webshooter.data.myentries.remote.SignupEntry

data class MyEntriesUiState(
    val groupedEntries: Map<String, List<SignupEntry>> = emptyMap(),
    val isLoading: Boolean = false
)
