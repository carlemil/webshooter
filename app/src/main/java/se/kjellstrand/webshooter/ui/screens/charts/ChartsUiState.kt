package se.kjellstrand.webshooter.ui.screens.charts

import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import se.kjellstrand.webshooter.data.charts.Participant
import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType

data class ShooterChartInfo(
    val name: String,
    val chartData: List<ChartDataPoint>
)

data class ChartsUiState(
    val chartData: Map<String, List<ChartDataPoint>> = emptyMap(),
    val comparedShooters: Map<Long, ShooterChartInfo> = emptyMap(),
    val selectedResultsType: String = "",
    val selectedWeaponClasses: Set<String> = emptySet(),
    val availableWeaponClasses: List<String> = emptyList(),
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
            if (selectedWeaponClasses.isEmpty()) return data
            return data.filter { it.weaponClass in selectedWeaponClasses }
        }

    val filteredComparedShooters: Map<Long, ShooterChartInfo>
        get() = comparedShooters.mapValues { (_, info) ->
            info.copy(chartData = info.chartData.filter {
                it.resultsType == selectedResultsType &&
                    (selectedWeaponClasses.isEmpty() || it.weaponClass in selectedWeaponClasses)
            })
        }

    val filteredClubMembers: List<ClubMember>
        get() {
            if (searchQuery.isBlank()) return clubMembers
            val query = searchQuery.lowercase()
            return allParticipants
                .filter { it.fullname.lowercase().contains(query) }
                .map { ClubMember(userId = it.userId, name = it.fullname, fullname = it.fullname) }
        }
}
