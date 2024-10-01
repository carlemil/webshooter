package se.kjellstrand.webshooter.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.ClassnameGeneral
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.common.GroupedItem
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
                            setGroupResults(resource.data.results)
                        )
                    }

                    is Resource.Error -> {
                        println("getResults Failed: ${resource.error}")
                    }

                    else -> {
                        println("getResults other state")
                    }
                }
            }
        }
    }

    private fun setGroupResults(results: List<Result>): List<GroupedItem> {
        val listOfGroupedItems = mutableListOf<GroupedItem>()

        val weaponClasses = results.map { it.weaponClass.classname }.distinct().sorted()

        weaponClasses.forEach { weaponClass ->
            val groupedResults =
                results.filter { it.weaponClass.classname == weaponClass }.sortedBy { it.placement }
            if (groupedResults.isNotEmpty()) {
                listOfGroupedItems.add(GroupedItem(weaponClass, groupedResults))
            }
        }
        return listOfGroupedItems
    }
}