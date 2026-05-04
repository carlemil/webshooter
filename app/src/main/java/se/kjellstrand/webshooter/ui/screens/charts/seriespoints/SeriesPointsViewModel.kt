package se.kjellstrand.webshooter.ui.screens.charts.seriespoints

import se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup

import kotlinx.coroutines.flow.StateFlow

interface SeriesPointsViewModel {
    val uiState: StateFlow<SeriesPointsUiState>
    fun selectShooter(userId: Long)
    fun selectWeaponGroup(group: WeaponClassGroup)
    fun selectYear(year: Int)
    fun setSearchQuery(query: String)
    fun setShowSearchDialog(show: Boolean)
    fun refresh()
}
