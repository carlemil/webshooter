package se.kjellstrand.webshooter.ui.signups

import se.kjellstrand.webshooter.data.signups.remote.SignupEntry

data class SignupsUiState(
    val groupedEntries: Map<String, List<SignupEntry>> = emptyMap(),
    val isLoading: Boolean = false
)
