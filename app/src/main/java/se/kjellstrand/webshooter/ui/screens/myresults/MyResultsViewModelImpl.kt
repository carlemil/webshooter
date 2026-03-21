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
import javax.inject.Inject

@HiltViewModel
class MyResultsViewModelImpl @Inject constructor(
    private val repository: SignupsRepository
) : ViewModel(), MyResultsViewModel {

    private val _uiState = MutableStateFlow(MyResultsUiState(isLoading = true))
    override val uiState: StateFlow<MyResultsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            repository.getSignups().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            groupedEntries = resource.data
                                .mapValues { (_, group) ->
                                    group.signups.filter { it.resultsPlacements != null }
                                }
                                .filterValues { it.isNotEmpty() }
                                .toSortedMap(compareByDescending { it }),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    else -> {}
                }
            }
        }
    }

    override fun reload() {
        _uiState.value = MyResultsUiState(isLoading = true)
        load()
    }
}
