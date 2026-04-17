package se.kjellstrand.webshooter.ui.screens.charts.seriespoints

import kotlinx.coroutines.flow.StateFlow

interface SeriesPointsViewModel {
    val uiState: StateFlow<SeriesPointsUiState>
    fun selectShooter(userId: Long)
    fun selectWeaponGroup(group: WeaponClassGroup)
    fun setSearchQuery(query: String)
    fun setShowSearchDialog(show: Boolean)
}
