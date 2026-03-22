package se.kjellstrand.webshooter.ui.screens.myresults

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.mysignups.SignupsRepository
import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry
import se.kjellstrand.webshooter.data.results.ResultsRepository
import javax.inject.Inject

@HiltViewModel
class MyResultsViewModelImpl @Inject constructor(
    private val signupsRepository: SignupsRepository,
    private val resultsRepository: ResultsRepository
) : ViewModel(), MyResultsViewModel {

    private val _uiState = MutableStateFlow(MyResultsUiState(isLoading = true))
    override val uiState: StateFlow<MyResultsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            signupsRepository.getSignups().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val groupedEntries = resource.data
                            .mapValues { (_, group) ->
                                group.signups.filter { it.resultsPlacements != null }
                            }
                            .filterValues { it.isNotEmpty() }
                            .toSortedMap(compareByDescending { it })
                        _uiState.value = _uiState.value.copy(
                            groupedEntries = groupedEntries,
                            isLoading = false
                        )
                        fetchResultStats(groupedEntries)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun fetchResultStats(groupedEntries: Map<String, List<SignupEntry>>) {
        val allEntries = groupedEntries.values.flatten()
        val competitionIds = allEntries.map { it.competition.id.toInt() }.distinct()
        competitionIds.forEach { compId ->
            viewModelScope.launch {
                val entriesForComp = allEntries.filter { it.competition.id.toInt() == compId }
                val newStats = mutableMapOf<Long, ResultStats>()
                resultsRepository.get(compId).collect { resource ->
                    if (resource is Resource.Success) {
                        resource.data.results.forEach { result ->
                            val entry = entriesForComp.firstOrNull { it.id == result.signupsID }
                            if (entry != null) {
                                newStats[entry.id] = ResultStats(
                                    stationCount = result.results.size,
                                    hits = result.hits,
                                    figureHits = result.figureHits
                                )
                            }
                        }
                    }
                }
                if (newStats.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        resultStats = _uiState.value.resultStats + newStats
                    )
                }
            }
        }
    }

    override fun reload() {
        _uiState.value = MyResultsUiState(isLoading = true)
        load()
    }
}
