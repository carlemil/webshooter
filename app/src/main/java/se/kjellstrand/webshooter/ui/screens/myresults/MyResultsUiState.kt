package se.kjellstrand.webshooter.ui.screens.myresults

import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry

data class MyResultsUiState(
    val groupedEntries: Map<String, List<SignupEntry>> = emptyMap(),
    val isLoading: Boolean = false
)
