package se.kjellstrand.webshooter.ui.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.remote.Result
import javax.inject.Inject

@HiltViewModel
open class ResultsViewModelImpl @Inject constructor(
    private val resultsRepository: ResultsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ResultsViewModel {

    override val competitionId: Int = checkNotNull(savedStateHandle["competitionId"])

    private val resultsType: ResultsType = try {
        ResultsType.valueOf(checkNotNull(savedStateHandle["resultsType"]))
    } catch (e: Exception) {
        ResultsType.FIELD
    }

    private val _uiState = MutableStateFlow(ResultsUiState(isLoading = true))
    override val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private val _resultsEvent = MutableSharedFlow<ResultsEvent>()
    override val resultsEvent: SharedFlow<ResultsEvent> = _resultsEvent.asSharedFlow()

    init {
        getResults(competitionId)
    }

    private fun getResults(competitionId: Int) {
        viewModelScope.launch {
            resultsRepository.get(competitionId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        if (resource.data.results.isEmpty()) {
                            _resultsEvent.emit(ResultsEvent.Empty)
                        } else {
                            println("getResults Success: ${resource.data}")
                            _uiState.value = ResultsUiState(
                                results = resource.data.results,
                                filterResults = filterResults(resource.data.results, resultsType),
                                groupedResults = groupResults(resource.data.results, resultsType),
                                allWeaponGroups = getWeaponGroups(resource.data.results).toList().sorted(),
                                selectedWeaponGroups = getWeaponGroups(resource.data.results),
                                isLoading = false,
                                resultsType = resultsType
                            )
                        }
                    }

                    is Resource.Error -> {
                        println("getResults Failed: ${resource.error}")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }

                    else -> {
                        println("getResults resource: $resource")
                    }
                }
            }
        }
    }

    override fun setMode(mode: Mode) {
        _uiState.update { currentState ->
            currentState.copy(mode = mode)
        }
    }

    override fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>) {
        _uiState.update { currentState ->
            currentState.copy(selectedWeaponGroups = selectedWeaponGroups)
        }

        val selectedGroups = _uiState.value.selectedWeaponGroups
        val filtered = if (selectedGroups.isEmpty()) {
            _uiState.value.results
        } else {
            _uiState.value.results.filter { result ->
                selectedGroups.contains(result.weaponClass.classname)
            }
        }.sortedByDescending { calculateSortOrder(it, resultsType) }
        _uiState.update { currentState ->
            currentState.copy(filterResults = filtered)
        }
    }

    companion object {
        fun groupResults(results: List<Result>, resultsType: ResultsType): List<GroupedItem> {
            val listOfGroupedItems = mutableListOf<GroupedItem>()

            val weaponClasses = results.map { it.weaponClass.classname }.distinct().sorted()

            weaponClasses.forEach { weaponClass ->
                val groupedResults =
                    results.filter { it.weaponClass.classname == weaponClass }
                        .sortedByDescending { calculateSortOrder(it, resultsType) }
                if (groupedResults.isNotEmpty()) {
                    listOfGroupedItems.add(GroupedItem(weaponClass, groupedResults))
                }
            }
            return listOfGroupedItems
        }

        fun filterResults(results: List<Result>, resultsType: ResultsType): List<Result> {
            val weaponClasses = getWeaponGroups(results)

            val listOfFilteredItems =
                results.filter {
                    it.weaponClass.classname in weaponClasses
                }.sortedByDescending { calculateSortOrder(it, resultsType) }
            return listOfFilteredItems
        }

        fun getWeaponGroups(results: List<Result>): Set<String> {
            return results.map { it.weaponClass.classname }.toSet()
        }

        fun calculateSortOrder(result: Result, resultsType: ResultsType): Int {
            return when (resultsType) {
                ResultsType.PRECISION -> (result.points * 1000000 + result.hits * 1000).toInt()
                else -> (result.hits * 1000000 + result.figureHits * 1000 + result.points).toInt()
            }
        }
    }
}