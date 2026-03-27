package se.kjellstrand.webshooter.ui.screens.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.mysignups.SignupsRepository
import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry
import se.kjellstrand.webshooter.data.results.ResultsRepository
import javax.inject.Inject

@HiltViewModel
class ChartViewModelImpl @Inject constructor(
    private val signupsRepository: SignupsRepository,
    private val resultsRepository: ResultsRepository
) : ViewModel(), ChartViewModel {

    private val _uiState = MutableStateFlow(ChartUiState(isLoading = true))
    override val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    // Written once in load(), before fetchStationCounts launches child coroutines.
    private var precisionEntries: List<SignupEntry> = emptyList()
    private var fieldEntries: List<SignupEntry> = emptyList()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            signupsRepository.getSignups().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val allEntries = resource.data.values
                            .flatMap { it.signups }
                            .filter { it.resultsPlacements != null }

                        precisionEntries = allEntries
                            .filter { it.competition.resultsType == "precision" }
                            .sortedBy { it.competition.date }

                        fieldEntries = allEntries
                            .filter { it.competition.resultsType == "field" }
                            .sortedBy { it.competition.date }

                        _uiState.update {
                            it.copy(
                                precisionDataPoints = buildDataPoints(precisionEntries, emptyMap()),
                                fieldDataPoints = buildDataPoints(fieldEntries, emptyMap()),
                                isLoading = false
                            )
                        }

                        fetchStationCounts(allEntries)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun fetchStationCounts(allEntries: List<SignupEntry>) {
        val competitionIds = allEntries.map { it.competition.id.toInt() }.distinct()
        competitionIds.forEach { compId ->
            viewModelScope.launch {
                val newCounts = mutableMapOf<Long, Int>()
                val entriesForComp = allEntries.filter { it.competition.id.toInt() == compId }
                resultsRepository.getPreferCached(compId).collect { resource ->
                    if (resource is Resource.Success) {
                        resource.data.results.forEach { result ->
                            val entry = entriesForComp.firstOrNull { it.id == result.signupsID }
                            if (entry != null) {
                                newCounts[entry.id] = result.results.size
                            }
                        }
                    }
                }
                if (newCounts.isNotEmpty()) {
                    _uiState.update { state ->
                        val combined = state.stationCounts + newCounts
                        state.copy(
                            precisionDataPoints = buildDataPoints(precisionEntries, combined),
                            fieldDataPoints = buildDataPoints(fieldEntries, combined),
                            stationCounts = combined
                        )
                    }
                }
            }
        }
    }

    private fun buildDataPoints(
        entries: List<SignupEntry>,
        stationCounts: Map<Long, Int>
    ): List<CompetitionDataPoint> = entries.map { entry ->
        val points = entry.resultsPlacements!!.points.toFloat()
        val stations = stationCounts[entry.id]?.takeIf { it > 0 }
        CompetitionDataPoint(
            date = entry.competition.date,
            competitionName = entry.competition.name,
            avgScorePerSerie = if (stations != null) points / stations else points
        )
    }
}
