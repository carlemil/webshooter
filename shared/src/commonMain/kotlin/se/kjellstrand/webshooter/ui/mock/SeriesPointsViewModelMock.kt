package se.kjellstrand.webshooter.ui.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup
import se.kjellstrand.webshooter.data.seriespoints.CompetitionSeries
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.SeriesPointsUiState
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.SeriesPointsViewModel

class SeriesPointsViewModelMock(
    initialState: SeriesPointsUiState = defaultState(),
) : SeriesPointsViewModel {
    override val uiState: StateFlow<SeriesPointsUiState> = MutableStateFlow(initialState)
    override fun selectShooter(userId: Long) {}
    override fun selectWeaponGroup(group: WeaponClassGroup) {}
    override fun selectYear(year: Int) {}
    override fun setSearchQuery(query: String) {}
    override fun setShowSearchDialog(show: Boolean) {}
    override fun refresh() {}
}

private fun defaultState(): SeriesPointsUiState = SeriesPointsUiState(
    currentUserId = 101L,
    selectedUserId = 101L,
    selectedUserName = "Erik Svensson",
    competitions = listOf(
        CompetitionSeries(
            competitionId = 301L,
            competitionName = "Vintercup",
            date = "2026-02-14",
            weaponClass = "C",
            seriesPoints = listOf(46L, 48L, 47L, 49L, 47L, 48L),
        ),
        CompetitionSeries(
            competitionId = 302L,
            competitionName = "Vårträff",
            date = "2026-04-22",
            weaponClass = "C",
            seriesPoints = listOf(44L, 46L, 47L, 45L, 48L, 47L),
        ),
        CompetitionSeries(
            competitionId = 303L,
            competitionName = "Sommarmatch",
            date = "2026-06-12",
            weaponClass = "C",
            seriesPoints = listOf(45L, 47L, 49L, 48L, 50L, 49L),
        ),
        CompetitionSeries(
            competitionId = 304L,
            competitionName = "Höststart",
            date = "2025-09-03",
            weaponClass = "C",
            seriesPoints = listOf(43L, 45L, 44L, 46L, 45L, 47L),
        ),
    ),
    selectedGroup = WeaponClassGroup.C,
    availableGroups = setOf(WeaponClassGroup.A, WeaponClassGroup.B, WeaponClassGroup.C),
    selectedYear = 0,
)
