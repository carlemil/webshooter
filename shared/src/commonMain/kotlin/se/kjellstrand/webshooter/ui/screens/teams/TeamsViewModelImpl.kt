package se.kjellstrand.webshooter.ui.screens.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitionteams.CompetitionTeamsRepository
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamEntry
import se.kjellstrand.webshooter.data.settings.SettingsRepository

class TeamsViewModelImpl(
    private val competitionId: Long,
    private val repository: CompetitionTeamsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel(), TeamsViewModel {

    private val _uiState = MutableStateFlow(CompetitionTeamsUiState(isLoading = true))
    override val uiState: StateFlow<CompetitionTeamsUiState> = _uiState.asStateFlow()

    init {
        loadTeams()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collect { resource ->
                if (resource is Resource.Success) {
                    val userId = resource.data.userId
                    _uiState.value = _uiState.value.copy(
                        currentUserId = userId,
                        teams = sortTeams(_uiState.value.teams, userId)
                    )
                }
            }
        }
    }

    private fun loadTeams() {
        viewModelScope.launch {
            repository.get(competitionId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value = _uiState.value.copy(
                        teams = sortTeams(resource.data.teams, _uiState.value.currentUserId),
                        isLoading = false
                    )
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun sortTeams(teams: List<TeamEntry>, currentUserId: Long?): List<TeamEntry> {
        return teams.sortedWith(
            compareByDescending<TeamEntry> { team ->
                currentUserId != null && team.signups.any { it.user.userId == currentUserId }
            }.thenBy { it.name }
        )
    }
}
