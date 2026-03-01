package se.kjellstrand.webshooter.ui.competitionpatrols

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitionpatrols.CompetitionPatrolsRepository
import javax.inject.Inject

@HiltViewModel
class CompetitionPatrolsViewModelImpl @Inject constructor(
    private val repository: CompetitionPatrolsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), CompetitionPatrolsViewModel {

    private val competitionId: Long = savedStateHandle["competitionId"] ?: -1L

    private val _uiState = MutableStateFlow(CompetitionPatrolsUiState(isLoading = true))
    override val uiState: StateFlow<CompetitionPatrolsUiState> = _uiState.asStateFlow()

    init {
        loadPatrols()
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

    override fun setFilterClub(club: String?) {
        _uiState.value = _uiState.value.copy(filterClub = club)
    }

    override fun setFilterWeaponGroup(group: String?) {
        _uiState.value = _uiState.value.copy(filterWeaponGroup = group)
    }

    override fun setSortField(field: PatrolsSortField) {
        val current = _uiState.value
        val newDirection = if (current.sortField == field) {
            if (current.sortDirection == PatrolsSortDirection.Ascending) PatrolsSortDirection.Descending
            else PatrolsSortDirection.Ascending
        } else {
            PatrolsSortDirection.Ascending
        }
        _uiState.value = current.copy(sortField = field, sortDirection = newDirection)
    }
}
