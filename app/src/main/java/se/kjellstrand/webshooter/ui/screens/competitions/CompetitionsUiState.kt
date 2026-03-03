package se.kjellstrand.webshooter.ui.screens.competitions

import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.Datum

data class CompetitionsUiState(
    val competitions: Competitions? = null,
    val isLoading: Boolean = false,
    val selectedCompetitionTypeIds: Set<Int> = emptySet(),
    val selectedStatuses: Set<String> = emptySet()
) {
    val allCompetitionTypes: List<CompetitionType>
        get() = competitions?.data?.map { it.competitionType }?.distinctBy { it.id } ?: emptyList()

    val allStatuses: List<Pair<String, String>>
        get() = competitions?.data?.map { it.status to it.statusHuman }?.distinctBy { it.first } ?: emptyList()

    val filteredData: List<Datum>
        get() {
            val data = competitions?.data ?: return emptyList()
            return data.filter { datum ->
                (selectedCompetitionTypeIds.isEmpty() || datum.competitionType.id in selectedCompetitionTypeIds) &&
                        (selectedStatuses.isEmpty() || datum.status in selectedStatuses)
            }
        }
}
