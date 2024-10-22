package se.kjellstrand.webshooter.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.remote.Result
import javax.inject.Inject

@HiltViewModel
open class ResultsViewModelImpl @Inject constructor(
    private val resultsRepository: ResultsRepository
) : ViewModel(), ResultsViewModel {

    private val _uiState = MutableStateFlow(ResultsUiState())
    override val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    init {
        getResults()
        _uiState.value = ResultsUiState()
    }

    private fun getResults() {
        viewModelScope.launch {
            resultsRepository.get().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        println("getResults Success: ${resource.data}")
                        _uiState.value = ResultsUiState(
                            resource.data.results,
                            filterResults(resource.data.results),
                            groupResults(resource.data.results),
                            getWeaponGroups(resource.data.results).toList().sorted(),
                            getWeaponGroups(resource.data.results)
                        )
                    }

                    is Resource.Error -> {
                        println("getResults Failed: ${resource.error}")
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
        }.sortedByDescending { calculateSortOrder(it) }
        _uiState.update { currentState ->
            currentState.copy(filterResults = filtered)
        }
    }

    companion object {
        fun groupResults(results: List<Result>): List<GroupedItem> {
            val listOfGroupedItems = mutableListOf<GroupedItem>()

            val weaponClasses = results.map { it.weaponClass.classname }.distinct().sorted()

            weaponClasses.forEach { weaponClass ->
                val groupedResults =
                    results.filter { it.weaponClass.classname == weaponClass }
                        .sortedBy { it.placement }
                if (groupedResults.isNotEmpty()) {
                    listOfGroupedItems.add(GroupedItem(weaponClass, groupedResults))
                }
            }
            return listOfGroupedItems
        }

        fun filterResults(results: List<Result>): List<Result> {
            val weaponClasses = getWeaponGroups(results)

            val listOfFilteredItems =
                results.filter {
                    it.weaponClass.classname in weaponClasses
                }.sortedByDescending { calculateSortOrder(it) }
            return listOfFilteredItems
        }

        fun getWeaponGroups(results: List<Result>): Set<String> {
            return results.map { it.weaponClass.classname }.toSet()
        }

        private fun calculateSortOrder(it: Result): Int {
            return (it.hits * 1000000 + it.figureHits * 1000 + it.points).toInt()
        }
    }
}