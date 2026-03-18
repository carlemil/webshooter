package se.kjellstrand.webshooter.ui.screens.mysignups

import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry

data class MySignupsUiState(
    val groupedEntries: Map<String, List<SignupEntry>> = emptyMap(),
    val isLoading: Boolean = false
)
