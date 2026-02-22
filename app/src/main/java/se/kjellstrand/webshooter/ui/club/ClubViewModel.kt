package se.kjellstrand.webshooter.ui.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.common.Resource
import javax.inject.Inject

@HiltViewModel
class ClubViewModel @Inject constructor(
    private val clubRepository: ClubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClubUiState(isLoading = true))
    val uiState: StateFlow<ClubUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            clubRepository.getUserClub().collect { resource ->
                when (resource) {
                    is Resource.Success -> _uiState.update {
                        it.copy(clubData = resource.data.club, isLoading = false, error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(error = resource.error.name, isLoading = false)
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = resource.isLoading) }
                }
            }
        }
    }

    fun selectTab(tab: ClubTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
