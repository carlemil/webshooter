package se.kjellstrand.webshooter.ui.competitions

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

    init {
        loadInitialPages()
    }

    private fun loadInitialPages() {
        loadCompetitions(1, 20)
        currentPage = 2
    }

    override fun loadNextPage() {
        if (_uiState.value.isLoading) return
        currentPage++
        loadCompetitions(currentPage, 10)
    }

    private fun loadCompetitions(page: Int, pageSize: Int) {
        val flow = competitionsRepository.get(page, pageSize)
        viewModelScope.launch {
            flow.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val currentCompetitions = _uiState.value.competitions?.data ?: emptyList()
                        val newCompetitions = resource.data.competitions.data
                        val allCompetitions = currentCompetitions + newCompetitions
                        _uiState.value = _uiState.value.copy(
                            competitions = resource.data.competitions.copy(data = allCompetitions),
                            isLoading = false
                        )
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }

                    else -> {
                        println("getCompetitions resource: $resource")
                    }
                }
            }
        }
    }

    override fun reload() {
        _uiState.value = _uiState.value.copy(competitions = null, isLoading = true)
        currentPage = 1
        loadInitialPages()
    }

    override fun getCompetitionById(competitionId: Long): Datum? {
        return uiState.value.competitions?.data?.find { it.id == competitionId }
    }
}
