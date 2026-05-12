package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.club.ClubTab
import se.kjellstrand.webshooter.ui.screens.club.ClubUiState
import se.kjellstrand.webshooter.ui.screens.club.ClubViewModel

class ClubViewModelMock(
    initialState: ClubUiState = ClubUiState(clubData = MockClub().clubData)
) : ClubViewModel {
    override val uiState: StateFlow<ClubUiState> = MutableStateFlow(initialState)
    override fun selectTab(tab: ClubTab) {}
}
