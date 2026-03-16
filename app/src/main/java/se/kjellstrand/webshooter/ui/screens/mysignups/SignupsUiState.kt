package se.kjellstrand.webshooter.ui.screens.mysignups

import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry

data class SignupsUiState(
    val groupedEntries: Map<String, List<SignupEntry>> = emptyMap(),
    val isLoading: Boolean = false
)
