package se.kjellstrand.webshooter.ui.screens.myresults

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
class MyResultsViewModelImpl @Inject constructor(
    private val signupsRepository: SignupsRepository,
    private val resultsRepository: ResultsRepository
) : ViewModel(), MyResultsViewModel {

    private val _uiState = MutableStateFlow(MyResultsUiState(isLoading = true))
    override val uiState: StateFlow<MyResultsUiState> = _uiState.asStateFlow()

    private var computeJob: Job? = null

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
                        computeDerivedData()
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
        val competitionIds = allEntries.map { it.competition.id }.distinct()
        if (competitionIds.isEmpty()) return
        _uiState.update { it.copy(isLoadingStats = true) }
        viewModelScope.launch {
            val jobs = competitionIds.map { compId ->
                launch {
                    val entriesForComp = allEntries.filter { it.competition.id == compId }
                    val newStats = mutableMapOf<Long, ResultStats>()
                    resultsRepository.getPreferCached(compId).collect { resource ->
                        if (resource is Resource.Success) {
                            resource.data.results.forEach { result ->
                                val entry =
                                    entriesForComp.firstOrNull { it.id == result.signupsID }
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
                        _uiState.update { it.copy(resultStats = it.resultStats + newStats) }
                        computeDerivedData()
                    }
                }
            }
            jobs.forEach { it.join() }
            _uiState.update { it.copy(isLoadingStats = false) }
        }
    }

    private fun computeDerivedData() {
        computeJob?.cancel()
        computeJob = viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            val allEntries = state.groupedEntries.values.flatten()

            val allTimeSummaryRows = buildSummaryRows(allEntries, state.resultStats)
            val yearlySummaryRows = state.groupedEntries.mapValues { (_, entries) ->
                buildSummaryRows(entries, state.resultStats)
            }
            val allCompetitions = allEntries
                .groupBy { it.competition.id }
                .values
                .sortedByDescending { it.first().competition.date }

            _uiState.update { current ->
                current.copy(
                    allTimeSummaryRows = allTimeSummaryRows,
                    yearlySummaryRows = yearlySummaryRows,
                    allCompetitions = allCompetitions
                )
            }
        }
    }

    private fun buildSummaryRows(
        entries: List<SignupEntry>,
        resultStats: Map<Long, ResultStats>
    ): List<Triple<String, List<SummaryRow>, SummaryRow>> {
        fun isRelevant(entry: SignupEntry): Boolean =
            entry.resultsPlacements != null ||
                (entry.competition.resultsTypeHuman == "Fält" && resultStats[entry.id] != null)

        val relevantTypes = entries.filter { isRelevant(it) }
            .map { it.competition.resultsTypeHuman }
            .distinct()
            .sortedBy { if (it == "Fält") 1 else 0 }

        val allRows = entries
            .filter { isRelevant(it) && it.competition.resultsTypeHuman in relevantTypes }
            .groupBy { it.weaponclass.classname to it.competition.resultsTypeHuman }
            .map { (key, group) ->
                val placements = group.mapNotNull { it.resultsPlacements }
                val avgScore = group.mapNotNull { entry ->
                    val rp = entry.resultsPlacements ?: return@mapNotNull null
                    val stations = resultStats[entry.id]?.stationCount?.takeIf { it > 0 }
                        ?: return@mapNotNull rp.points.toDouble()
                    rp.points.toDouble() / stations
                }.let { if (it.isEmpty()) 0.0 else it.average() }
                val hits = group.mapNotNull { entry -> resultStats[entry.id]?.hits }
                val figureHits = group.mapNotNull { entry -> resultStats[entry.id]?.figureHits }
                val avgHits = group.mapNotNull { entry ->
                    val stations = resultStats[entry.id]?.stationCount?.takeIf { it > 0 }
                        ?: return@mapNotNull null
                    resultStats[entry.id]!!.hits.toDouble() / stations
                }.let { if (it.isEmpty()) 0.0 else it.average() }
                val avgFigureHits = if (figureHits.isEmpty()) 0.0 else figureHits.map { it.toDouble() }.average()
                SummaryRow(
                    weaponClass = key.first,
                    competitionType = key.second,
                    count = group.size,
                    avgScore = avgScore,
                    avgHits = avgHits,
                    avgX = avgHits,
                    figureHits = avgFigureHits,
                    totalScore = group.sumOf { it.resultsPlacements?.points?.toDouble() ?: 0.0 },
                    totalHits = hits.sumOf { it.toInt() },
                    totalFigureHits = figureHits.sumOf { it.toInt() },
                    medalScore = placements.sumOf {
                        when (it.stdMedal) { "B" -> 1; "S" -> 2; else -> 0 }.toInt()
                    }
                )
            }

        return relevantTypes.mapNotNull { type ->
            val rows = allRows.filter { it.competitionType == type }.sortedBy { it.weaponClass }
            if (rows.isEmpty()) null else {
                val typeEntries = entries.filter { isRelevant(it) && it.competition.resultsTypeHuman == type }
                val placements = typeEntries.mapNotNull { it.resultsPlacements }
                val avgScore = typeEntries.mapNotNull { entry ->
                    val rp = entry.resultsPlacements ?: return@mapNotNull null
                    val stations = resultStats[entry.id]?.stationCount?.takeIf { it > 0 }
                        ?: return@mapNotNull rp.points.toDouble()
                    rp.points.toDouble() / stations
                }.let { if (it.isEmpty()) 0.0 else it.average() }
                val hits = typeEntries.mapNotNull { entry -> resultStats[entry.id]?.hits }
                val figureHits = typeEntries.mapNotNull { entry -> resultStats[entry.id]?.figureHits }
                val avgHits = typeEntries.mapNotNull { entry ->
                    val stations = resultStats[entry.id]?.stationCount?.takeIf { it > 0 }
                        ?: return@mapNotNull null
                    resultStats[entry.id]!!.hits.toDouble() / stations
                }.let { if (it.isEmpty()) 0.0 else it.average() }
                val avgFigureHits = if (figureHits.isEmpty()) 0.0 else figureHits.map { it.toDouble() }.average()
                val totalRow = SummaryRow(
                    weaponClass = rows.joinToString(", ") { it.weaponClass },
                    competitionType = type,
                    count = typeEntries.size,
                    avgScore = avgScore,
                    avgHits = avgHits,
                    avgX = avgHits,
                    figureHits = avgFigureHits,
                    totalScore = typeEntries.sumOf { it.resultsPlacements?.points?.toDouble() ?: 0.0 },
                    totalHits = hits.sumOf { it.toInt() },
                    totalFigureHits = figureHits.sumOf { it.toInt() },
                    medalScore = placements.sumOf {
                        when (it.stdMedal) { "B" -> 1; "S" -> 2; else -> 0 }.toInt()
                    }
                )
                Triple(type, rows, totalRow)
            }
        }
    }

    override fun reload() {
        _uiState.value = MyResultsUiState(isLoading = true)
        load()
    }
}
