package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ChartsUiState
import se.kjellstrand.webshooter.ui.screens.charts.resulttrends.ResultsTrendsViewModel
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.WeaponClassGroup

class ChartsViewModelMock(
    initialState: ChartsUiState = ChartsUiState()
) : ResultsTrendsViewModel {
    override val uiState: StateFlow<ChartsUiState> = MutableStateFlow(initialState)
    override fun selectTab(resultsType: String) {}
    override fun selectWeaponGroup(group: WeaponClassGroup?) {}
    override fun addShooter(userId: Long) {}
    override fun removeShooter(userId: Long) {}
    override fun setSearchQuery(query: String) {}
    override fun setShowSearchDialog(show: Boolean) {}
    override fun refresh() {}
}
