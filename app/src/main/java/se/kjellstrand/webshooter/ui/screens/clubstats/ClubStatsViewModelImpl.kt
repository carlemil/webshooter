package se.kjellstrand.webshooter.ui.screens.clubstats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.clubstats.ClubStatsRepository
import se.kjellstrand.webshooter.data.common.Resource
import javax.inject.Inject

@HiltViewModel
class ClubStatsViewModelImpl @Inject constructor(
    private val clubStatsRepository: ClubStatsRepository
) : ViewModel(), ClubStatsViewModel {

    private val _uiState = MutableStateFlow(ClubStatsUiState(isLoading = true))
    override val uiState: StateFlow<ClubStatsUiState> = _uiState.asStateFlow()

    init {
        loadClubStats()
    }

    private fun loadClubStats() {
        viewModelScope.launch {
            clubStatsRepository.getClubStats().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            shooterStats = resource.data.shooterStats,
                            hasError = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            hasError = true
                        )
                    }
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = resource.isLoading
                        )
                    }
                }
            }
        }
    }
}
