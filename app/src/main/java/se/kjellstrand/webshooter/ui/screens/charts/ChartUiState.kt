package se.kjellstrand.webshooter.ui.screens.charts

data class CompetitionDataPoint(
    val date: String,
    val competitionName: String,
    val avgScorePerSerie: Float
)

data class ChartUiState(
    val precisionDataPoints: List<CompetitionDataPoint> = emptyList(),
    val fieldDataPoints: List<CompetitionDataPoint> = emptyList(),
    val stationCounts: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = false
)
