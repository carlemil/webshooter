package se.kjellstrand.webshooter.ui.screens.competitionsignups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitionsignups.CompetitionSignupsRepository
import javax.inject.Inject

@HiltViewModel
class CompetitionSignupsViewModelImpl @Inject constructor(
    private val repository: CompetitionSignupsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), CompetitionSignupsViewModel {

    private val competitionId: Long = savedStateHandle["competitionId"] ?: -1L

    private val _uiState = MutableStateFlow(CompetitionSignupsUiState(isLoading = true))
    override val uiState: StateFlow<CompetitionSignupsUiState> = _uiState.asStateFlow()

    private var currentPage = 0

    init {
        loadPage(1)
    }

    override fun loadNextPage() {
        if (_uiState.value.isLoading) return
        val state = _uiState.value
        if (state.currentPage >= state.totalPages) return
        loadPage(state.currentPage + 1)
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            repository.get(competitionId, page, 100).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val paged = resource.data.signups
                        val accumulated = _uiState.value.allSignups + paged.data
                        _uiState.value = _uiState.value.copy(
                            allSignups = accumulated,
                            currentPage = paged.currentPage,
                            totalPages = paged.lastPage,
                            total = paged.total,
                            isLoading = false
                        )
                        if (paged.currentPage < paged.lastPage) {
                            loadPage(paged.currentPage + 1)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
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

    override fun setSortField(field: SignupsListSortField) {
        val current = _uiState.value
        val newDirection = if (current.sortField == field) {
            if (current.sortDirection == SortDirection.Ascending) SortDirection.Descending
            else SortDirection.Ascending
        } else {
            SortDirection.Ascending
        }
        _uiState.value = current.copy(sortField = field, sortDirection = newDirection)
    }
}
