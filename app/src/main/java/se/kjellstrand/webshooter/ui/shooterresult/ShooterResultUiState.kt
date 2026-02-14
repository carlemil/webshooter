package se.kjellstrand.webshooter.ui.shooterresult

import se.kjellstrand.webshooter.data.results.remote.Result

data class ShooterResultUiState(
    val isLoading: Boolean = true,
    val shooterName: String = "",
    val results: List<Result> = emptyList(),
    val error: String? = null
)