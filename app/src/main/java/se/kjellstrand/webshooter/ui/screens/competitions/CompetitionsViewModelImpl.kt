package se.kjellstrand.webshooter.ui.screens.competitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import javax.inject.Inject

@HiltViewModel
class CompetitionsViewModelImpl @Inject constructor(
    private val competitionsRepository: CompetitionsRepository
) : ViewModel(), CompetitionsViewModel {

    private val _uiState = MutableStateFlow(CompetitionsUiState(isLoading = true))
    override val uiState: StateFlow<CompetitionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            competitionsRepository.observeAll().collect { data ->
                _uiState.value = _uiState.value.copy(
                    competitions = Competitions(
                        currentPage = 1,
                        data = data,
                        lastPage = 1,
                        total = data.size.toLong(),
                        status = "",
                        competitionTypes = emptyList()
                    ),
                    isLoading = false,
                    hasError = false
                )
            }
        }
    }

    override fun reload() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, hasError = false)
            try {
                competitionsRepository.syncAll()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasError = _uiState.value.competitions == null
                )
            }
        }
    }

    override fun getCompetitionById(competitionId: Long): Datum? {
        return uiState.value.competitions?.data?.find { it.id == competitionId }
    }

    override fun setSelectedCompetitionTypeIds(ids: Set<Int>) {
        _uiState.value = _uiState.value.copy(selectedCompetitionTypeIds = ids)
    }

    override fun setSelectedStatuses(statuses: Set<String>) {
        _uiState.value = _uiState.value.copy(selectedStatuses = statuses)
    }
}
