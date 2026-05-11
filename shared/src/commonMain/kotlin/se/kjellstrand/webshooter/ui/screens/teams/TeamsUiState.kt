package se.kjellstrand.webshooter.ui.screens.teams

import se.kjellstrand.webshooter.data.competitionteams.remote.TeamEntry

data class CompetitionTeamsUiState(
    val teams: List<TeamEntry> = emptyList(),
    val isLoading: Boolean = false,
    val currentUserId: Long? = null
)
