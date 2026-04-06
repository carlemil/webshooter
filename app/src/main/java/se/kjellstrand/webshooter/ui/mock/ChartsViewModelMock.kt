package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.charts.ChartsUiState
import se.kjellstrand.webshooter.ui.screens.charts.ChartsViewModel

class ChartsViewModelMock(
    initialState: ChartsUiState = ChartsUiState()
) : ChartsViewModel {
    override val uiState: StateFlow<ChartsUiState> = MutableStateFlow(initialState)
    override fun selectTab(resultsType: String) {}
    override fun toggleWeaponClass(weaponClass: String) {}
    override fun addShooter(userId: Long) {}
    override fun removeShooter(userId: Long) {}
    override fun setSearchQuery(query: String) {}
    override fun setShowSearchDialog(show: Boolean) {}
}
