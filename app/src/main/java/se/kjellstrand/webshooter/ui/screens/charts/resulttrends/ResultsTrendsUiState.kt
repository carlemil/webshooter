package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import se.kjellstrand.webshooter.data.charts.Participant
import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.WeaponClassGroup


data class ShooterChartInfo(
    val name: String,
    val chartData: List<ChartDataPoint>
)

data class ChartsUiState(
    val chartData: Map<String, List<ChartDataPoint>> = emptyMap(),
    val comparedShooters: Map<Long, ShooterChartInfo> = emptyMap(),
    val selectedResultsType: String = "",
    val selectedGroup: WeaponClassGroup? = WeaponClassGroup.C,
    val availableGroups: Set<WeaponClassGroup> = setOf(
        WeaponClassGroup.A,
        WeaponClassGroup.B,
        WeaponClassGroup.C
    ),
    val availableResultsTypes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val clubMembers: List<ClubMember> = emptyList(),
    val allParticipants: List<Participant> = emptyList(),
    val searchQuery: String = "",
    val showSearchDialog: Boolean = false
) {
    val filteredChartData: List<ChartDataPoint>
        get() {
            val data = chartData[selectedResultsType] ?: return emptyList()
            val group = selectedGroup ?: return data
            return data.filter { group.matches(it.weaponClass) }
        }

    val filteredComparedShooters: Map<Long, ShooterChartInfo>
        get() = comparedShooters
            .mapValues { (_, info) ->
                info.copy(chartData = info.chartData.filter {
                    it.resultsType == selectedResultsType &&
                        (selectedGroup?.matches(it.weaponClass) ?: true)
                })
            }
            .toList()
            .sortedBy { (_, info) -> info.name.lowercase() }
            .toMap()

    val relevantUserIds: Set<Long>
        get() = allParticipants.map { it.userId }.toSet()

    val myAverage: Float?
        get() = filteredChartData.takeIf { it.isNotEmpty() }
            ?.map { it.averageSerieScore.toFloat() }
            ?.average()?.toFloat()

    data class TrendLine(
        val fromX: Float,
        val fromY: Float,
        val toX: Float,
        val toY: Float,
    )

    val myTrend: TrendLine?
        get() {
            val points = filteredChartData.sortedBy { it.date }
            if (points.size < 2) return null
            val xs = points.indices.map { it.toFloat() }
            val ys = points.map { it.averageSerieScore.toFloat() }
            val meanX = xs.average().toFloat()
            val meanY = ys.average().toFloat()
            val num = xs.zip(ys)
                .sumOf { (x, y) -> ((x - meanX) * (y - meanY)).toDouble() }
                .toFloat()
            val den = xs
                .sumOf { ((it - meanX) * (it - meanX)).toDouble() }
                .toFloat()
            if (den == 0f) return null
            val slope = num / den
            val intercept = meanY - slope * meanX
            val firstX = xs.first()
            val lastX = xs.last()
            return TrendLine(
                fromX = firstX,
                fromY = slope * firstX + intercept,
                toX = lastX,
                toY = slope * lastX + intercept
            )
        }
}
