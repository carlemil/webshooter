package se.kjellstrand.webshooter.ui.screens.myresults

import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry

data class SummaryRow(
    val weaponClass: String,
    val competitionType: String,
    val count: Int,
    val avgScore: Double,
    val avgHits: Double,
    val avgX: Double,
    val figureHits: Double,
    val totalScore: Double,
    val totalHits: Int,
    val totalFigureHits: Int,
    val medalScore: Int
)

data class ResultStats(
    val stationCount: Int,
    val hits: Long,
    val figureHits: Long
)

data class MyResultsUiState(
    val groupedEntries: Map<String, List<SignupEntry>> = emptyMap(),
    val resultStats: Map<Long, ResultStats> = emptyMap(),
    val allTimeSummaryRows: List<Triple<String, List<SummaryRow>, SummaryRow>> = emptyList(),
    val yearlySummaryRows: Map<String, List<Triple<String, List<SummaryRow>, SummaryRow>>> = emptyMap(),
    val allCompetitions: List<List<SignupEntry>> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingStats: Boolean = false
)
