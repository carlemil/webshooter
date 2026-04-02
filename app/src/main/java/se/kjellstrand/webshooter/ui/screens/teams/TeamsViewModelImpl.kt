package se.kjellstrand.webshooter.ui.screens.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitionteams.CompetitionTeamsRepository
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class TeamsViewModelImpl @Inject constructor(
    private val repository: CompetitionTeamsRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), TeamsViewModel {

    private val competitionId: Long = savedStateHandle["competitionId"] ?: -1L

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
                    _uiState.value = _uiState.value.copy(currentUserId = resource.data.userId)
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
                        teams = resource.data.teams.sortedBy { it.name },
                        isLoading = false
                    )
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}
