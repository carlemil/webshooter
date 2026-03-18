package se.kjellstrand.webshooter.ui.screens.signups

import kotlinx.coroutines.flow.StateFlow

interface SignupsViewModel {
    val uiState: StateFlow<CompetitionSignupsUiState>
    fun loadNextPage()
    fun setFilterClub(club: String?)
    fun setFilterWeaponGroup(group: String?)
    fun setSortField(field: SignupsListSortField)
}
