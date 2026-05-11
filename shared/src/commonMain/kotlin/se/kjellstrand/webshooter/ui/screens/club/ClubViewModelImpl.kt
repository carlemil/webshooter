package se.kjellstrand.webshooter.ui.screens.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.common.Resource

class ClubViewModelImpl(
    private val clubRepository: ClubRepository,
) : ViewModel(), ClubViewModel {

    private val _uiState = MutableStateFlow(ClubUiState(isLoading = true))
    override val uiState: StateFlow<ClubUiState> = _uiState.asStateFlow()

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
                        it.copy(error = resource.error::class.simpleName, isLoading = false)
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = resource.isLoading) }
                }
            }
        }
    }

    override fun selectTab(tab: ClubTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
