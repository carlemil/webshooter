package se.kjellstrand.webshooter.ui.screens.patrols

import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolEntry

enum class PatrolsSortField { Name, Club, WeaponGroup }
enum class PatrolsSortDirection { Ascending, Descending }

data class CompetitionPatrolsUiState(
    val patrols: List<PatrolEntry> = emptyList(),
    val isLoading: Boolean = false,
    val competitionTypeId: Int = 0,
    val sortDirection: PatrolsSortDirection = PatrolsSortDirection.Ascending,
    val currentUserId: Long? = null
)
