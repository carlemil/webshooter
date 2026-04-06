package se.kjellstrand.webshooter.ui.screens.charts

import kotlinx.coroutines.flow.StateFlow

interface ChartsViewModel {
    val uiState: StateFlow<ChartsUiState>
    fun selectTab(resultsType: String)
    fun toggleWeaponClass(weaponClass: String)
    fun addShooter(userId: Long)
    fun removeShooter(userId: Long)
    fun setSearchQuery(query: String)
    fun setShowSearchDialog(show: Boolean)
}
