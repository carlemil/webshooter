package se.kjellstrand.webshooter.ui.myentries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import javax.inject.Inject

@HiltViewModel
class MyEntriesViewModel @Inject constructor(
    private val competitionsRepository: CompetitionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyEntriesUiState())
    val uiState: StateFlow<MyEntriesUiState> = _uiState.asStateFlow()

    private var nextApiPage = 1
    private var lastApiPage = Int.MAX_VALUE
    private var isFetching = false

    init {
        fetchPages(targetNewEntries = 20)
    }

    fun loadMore() {
        if (isFetching || nextApiPage > lastApiPage) return
        fetchPages(targetNewEntries = 10)
    }

    fun retry() {
        _uiState.value = MyEntriesUiState()
        nextApiPage = 1
        lastApiPage = Int.MAX_VALUE
        isFetching = false
        fetchPages(targetNewEntries = 20)
    }

    private fun fetchPages(targetNewEntries: Int) {
        isFetching = true
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val accumulated = _uiState.value.entries.toMutableList()
            val startSize = accumulated.size

            while (accumulated.size - startSize < targetNewEntries && nextApiPage <= lastApiPage) {
                var errorOccurred = false
                competitionsRepository.getMyEntriesPage(nextApiPage).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            lastApiPage = resource.data.competitions.lastPage.toInt()
                            accumulated.addAll(
                                resource.data.competitions.data.filter { it.userSignups.isNotEmpty() }
                            )
                            nextApiPage++
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Failed to load entries"
                            )
                            errorOccurred = true
                        }
                        is Resource.Loading -> {}
                    }
                }
                if (errorOccurred) {
                    isFetching = false
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(
                entries = accumulated.sortedByDescending { it.date },
                isLoading = false,
                hasMore = nextApiPage <= lastApiPage
            )
            isFetching = false
        }
    }
}
