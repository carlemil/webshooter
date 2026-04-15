package se.kjellstrand.webshooter.data.results.local

data class ChartPointRow(
    val competitionId: Long,
    val competitionName: String,
    val date: String,
    val resultsType: String,
    val weaponClassName: String,
    val userId: Long,
    val averageScore: Double
)

data class ParticipantRow(
    val userId: Long,
    val fullname: String
)

data class SeriesRow(
    val competitionId: Long,
    val competitionName: String,
    val date: String,
    val weaponClassName: String,
    val stationResultsJson: String
)
