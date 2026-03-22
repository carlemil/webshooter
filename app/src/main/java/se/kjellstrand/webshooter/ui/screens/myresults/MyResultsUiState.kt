package se.kjellstrand.webshooter.ui.screens.myresults

import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry

data class ResultStats(
    val stationCount: Int,
    val hits: Long,
    val figureHits: Long
)

data class MyResultsUiState(
    val groupedEntries: Map<String, List<SignupEntry>> = emptyMap(),
    val resultStats: Map<Long, ResultStats> = emptyMap(), // keyed by SignupEntry.id
    val isLoading: Boolean = false
)
