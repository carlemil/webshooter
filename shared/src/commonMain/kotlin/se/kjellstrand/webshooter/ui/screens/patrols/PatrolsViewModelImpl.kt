package se.kjellstrand.webshooter.ui.screens.patrols

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitionpatrols.CompetitionPatrolsRepository
import se.kjellstrand.webshooter.data.settings.SettingsRepository

class PatrolsViewModelImpl(
    private val competitionId: Long,
    private val competitionTypeId: Int,
    private val repository: CompetitionPatrolsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel(), PatrolsViewModel {

    private val _uiState = MutableStateFlow(CompetitionPatrolsUiState(isLoading = true, competitionTypeId = competitionTypeId))
    override val uiState: StateFlow<CompetitionPatrolsUiState> = _uiState.asStateFlow()

    init {
        loadPatrols()
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

    private fun loadPatrols() {
        viewModelScope.launch {
            repository.get(competitionId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value = _uiState.value.copy(
                        patrols = resource.data.patrols.sortedBy { it.sortorder },
                        isLoading = false
                    )
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}
