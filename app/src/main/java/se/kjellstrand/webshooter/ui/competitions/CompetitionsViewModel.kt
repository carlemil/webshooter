package se.kjellstrand.webshooter.ui.competitions

import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.common.CompetitionStatus
import se.kjellstrand.webshooter.data.competitions.remote.Datum

interface CompetitionsViewModel {
    val uiState: StateFlow<CompetitionsUiState>
    fun getCompetitionById(competitionId: Long): Datum?
    fun loadNextPage()
    fun setCompetitionStatus(status: CompetitionStatus)
}
