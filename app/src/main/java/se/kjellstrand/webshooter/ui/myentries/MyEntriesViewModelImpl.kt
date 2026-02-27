package se.kjellstrand.webshooter.ui.myentries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.myentries.MyEntriesRepository
import javax.inject.Inject

@HiltViewModel
class MyEntriesViewModelImpl @Inject constructor(
    private val repository: MyEntriesRepository
) : ViewModel(), MyEntriesViewModel {

    private val _uiState = MutableStateFlow(MyEntriesUiState(isLoading = true))
    override val uiState: StateFlow<MyEntriesUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var totalPages = Int.MAX_VALUE

    init {
        loadAllPages()
    }

    private fun loadAllPages() {
        viewModelScope.launch {
            while (currentPage < totalPages) {
                currentPage++
                var pageFinished = false
                repository.getPage(currentPage, 50).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val comps = resource.data.competitions
                            totalPages = comps.lastPage.toInt()
                            val newEntries = comps.data.filter { it.userSignups.isNotEmpty() }
                            _uiState.value = _uiState.value.copy(
                                entries = _uiState.value.entries + newEntries,
                                isLoading = currentPage < totalPages
                            )
                            pageFinished = true
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(isLoading = false, isFinished = true)
                            totalPages = 0
                            pageFinished = true
                        }
                        else -> {}
                    }
                }
                if (!pageFinished) break
            }
            _uiState.value = _uiState.value.copy(isLoading = false, isFinished = true)
        }
    }

    override fun reload() {
        currentPage = 0
        totalPages = Int.MAX_VALUE
        _uiState.value = MyEntriesUiState(isLoading = true)
        loadAllPages()
    }
}
