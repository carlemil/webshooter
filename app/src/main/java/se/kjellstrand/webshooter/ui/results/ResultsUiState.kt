package se.kjellstrand.webshooter.ui.results

import se.kjellstrand.webshooter.data.results.remote.Result

data class ResultsUiState(
    val results: List<Result>? = null
)