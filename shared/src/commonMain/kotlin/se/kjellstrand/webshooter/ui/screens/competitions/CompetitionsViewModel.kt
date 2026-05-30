package se.kjellstrand.webshooter.ui.screens.competitions

import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.competitions.remote.Datum

interface CompetitionsViewModel {
    val uiState: StateFlow<CompetitionsUiState>
    val savedFirstVisibleItemIndex: Int
    val savedFirstVisibleItemScrollOffset: Int
    fun getCompetitionById(competitionId: Long): Datum?
    fun reload()
    fun onScreenOpened()
    fun setSelectedStatuses(statuses: Set<String>)
    fun saveScrollPosition(index: Int, offset: Int)
}
