package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.signups.CompetitionSignupsUiState
import se.kjellstrand.webshooter.ui.screens.signups.SignupsListSortField
import se.kjellstrand.webshooter.ui.screens.signups.SignupsViewModel

class SignupsViewModelMock(
    initialState: CompetitionSignupsUiState = CompetitionSignupsUiState(
        allSignups = MockSignups().signups
    )
) : SignupsViewModel {
    override val uiState: StateFlow<CompetitionSignupsUiState> = MutableStateFlow(initialState)
    override fun loadNextPage() {}
    override fun toggleFilterClub(club: String) {}
    override fun clearFilterClubs() {}
    override fun toggleFilterWeaponGroup(group: String) {}
    override fun clearFilterWeaponGroups() {}
    override fun setSortField(field: SignupsListSortField) {}
}
