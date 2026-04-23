package se.kjellstrand.webshooter.ui.screens.competitions

import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.Datum

data class CompetitionsUiState(
    val competitions: Competitions? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val selectedStatuses: Set<String> = emptySet()
) {
    val allStatuses: Map<String, String>
        get() = competitions?.data?.associate { it.status to it.statusHuman }.orEmpty()

    val filteredData: List<Datum>
        get() {
            val data = competitions?.data ?: return emptyList()
            return data.filter { datum ->
                selectedStatuses.isEmpty() || datum.status in selectedStatuses
            }
        }
}
