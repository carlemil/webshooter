package se.kjellstrand.webshooter.ui.competitionpatrols

import kotlinx.coroutines.flow.StateFlow

interface CompetitionPatrolsViewModel {
    val uiState: StateFlow<CompetitionPatrolsUiState>
    fun setFilterClub(club: String?)
    fun setFilterWeaponGroup(group: String?)
    fun setSortField(field: PatrolsSortField)
}
