package se.kjellstrand.webshooter.ui.screens.competitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import javax.inject.Inject

@HiltViewModel
class CompetitionsViewModelImpl @Inject constructor(
    private val competitionsRepository: CompetitionsRepository
) : ViewModel(), CompetitionsViewModel {

    private val _uiState = MutableStateFlow(CompetitionsUiState(isLoading = true))
    override val uiState: StateFlow<CompetitionsUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var reachedCompleted = false

    init {
        loadInitialPages()
    }

    private fun loadInitialPages() {
        loadCompetitions(1, 20)
        currentPage = 2
    }

    override fun loadNextPage() {
        if (_uiState.value.isLoading || reachedCompleted) return
        _uiState.value = _uiState.value.copy(isLoading = true)
        currentPage++
        loadCompetitions(currentPage, 10)
    }

    private fun loadCompetitions(page: Int, pageSize: Int) {
        val flow = competitionsRepository.get(page, pageSize)
        viewModelScope.launch {
            flow.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val allCompleted = resource.data.competitions.data.all { it.status == "completed" }
                        if (page == 1) {
                            _uiState.value = _uiState.value.copy(
                                competitions = resource.data.competitions,
                                isLoading = false
                            )
                        } else {
                            val currentCompetitions = _uiState.value.competitions?.data ?: emptyList()
                            val newCompetitions = resource.data.competitions.data
                            _uiState.value = _uiState.value.copy(
                                competitions = resource.data.competitions.copy(data = currentCompetitions + newCompetitions),
                                isLoading = false
                            )
                        }
                        if (allCompleted && page > 1) {
                            reachedCompleted = true
                        }
                    }

                    is Resource.Error -> {
                        if (page > 1) currentPage--
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            hasError = _uiState.value.competitions == null
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    override fun reload() {
        _uiState.value = _uiState.value.copy(competitions = null, isLoading = true, hasError = false)
        currentPage = 1
        reachedCompleted = false
        loadInitialPages()
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
