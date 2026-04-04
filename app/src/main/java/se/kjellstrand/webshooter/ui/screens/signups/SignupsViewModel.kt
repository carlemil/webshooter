package se.kjellstrand.webshooter.ui.screens.signups

import kotlinx.coroutines.flow.StateFlow

interface SignupsViewModel {
    val uiState: StateFlow<CompetitionSignupsUiState>
    fun loadNextPage()
    fun toggleFilterClub(club: String)
    fun clearFilterClubs()
    fun toggleFilterWeaponGroup(group: String)
    fun clearFilterWeaponGroups()
    fun setSortField(field: SignupsListSortField)
}
