package se.kjellstrand.webshooter.ui.screens.charts.clubstats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.clubstats.ClubStatsRepository
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup
import java.time.Year
import javax.inject.Inject

@HiltViewModel
class ClubStatsViewModelImpl @Inject constructor(
    private val clubStatsRepository: ClubStatsRepository
) : ViewModel(), ClubStatsViewModel {

    private val _uiState = MutableStateFlow(
        ClubStatsUiState(isLoading = true, year = Year.now().value - 1)
    )
    override val uiState: StateFlow<ClubStatsUiState> = _uiState.asStateFlow()

    init {
        loadClubStats(_uiState.value.selectedGroup, _uiState.value.year)
    }

    override fun selectWeaponGroup(group: WeaponClassGroup?) {
        if (_uiState.value.selectedGroup == group) return
        _uiState.value = _uiState.value.copy(
            selectedGroup = group,
            isLoading = true,
            shooterStats = emptyList()
        )
        loadClubStats(group, _uiState.value.year)
    }

    override fun selectYear(year: Int) {
        if (_uiState.value.year == year) return
        _uiState.value = _uiState.value.copy(
            year = year,
            isLoading = true,
            shooterStats = emptyList()
        )
        loadClubStats(_uiState.value.selectedGroup, year)
    }

    override fun refresh() {
        loadClubStats(_uiState.value.selectedGroup, _uiState.value.year)
    }

    private fun loadClubStats(selectedGroup: WeaponClassGroup?, year: Int) {
        val yearParam = if (year == 0) null else year
        viewModelScope.launch {
            clubStatsRepository.getClubStats(selectedGroup, yearParam).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            shooterStats = resource.data.shooterStats,
                            availableYears = resource.data.availableYears,
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
