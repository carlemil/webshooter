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

    init {
        load()
    }

    fun load() {
        _uiState.value = MyEntriesUiState(isLoading = true)
        viewModelScope.launch {
            competitionsRepository.getMyEntries().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = resource.isLoading)
                    is Resource.Success -> _uiState.value = _uiState.value.copy(
                        entries = resource.data,
                        isLoading = false
                    )
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load entries"
                    )
                }
            }
        }
    }
}
