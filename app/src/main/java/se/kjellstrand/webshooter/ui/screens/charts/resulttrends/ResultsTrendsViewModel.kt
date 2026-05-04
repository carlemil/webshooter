package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup

interface ResultsTrendsViewModel {
    val uiState: StateFlow<ChartsUiState>
    fun selectTab(resultsType: String)
    fun selectWeaponGroup(group: WeaponClassGroup?)
    fun addShooter(userId: Long)
    fun removeShooter(userId: Long)
    fun setSearchQuery(query: String)
    fun setShowSearchDialog(show: Boolean)
    fun refresh()
}
