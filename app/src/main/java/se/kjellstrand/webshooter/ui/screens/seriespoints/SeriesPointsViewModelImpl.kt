package se.kjellstrand.webshooter.ui.screens.seriespoints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.seriespoints.SeriesPointsRepository
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SeriesPointsViewModelImpl @Inject constructor(
    private val seriesPointsRepository: SeriesPointsRepository,
    private val settingsRepository: SettingsRepository,
    private val clubRepository: ClubRepository
) : ViewModel(), SeriesPointsViewModel {

    private val _uiState = MutableStateFlow(SeriesPointsUiState(isLoading = true))
    override val uiState: StateFlow<SeriesPointsUiState> = _uiState.asStateFlow()

    init {
        loadUserAndData()
        loadClubMembers()
    }

    private fun loadUserAndData() {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val userId = resource.data.userId
                        val name = resource.data.fullname
                        _uiState.value = _uiState.value.copy(
                            currentUserId = userId,
                            selectedUserId = userId,
                            selectedUserName = name
                        )
                        loadSeriesData(userId)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            hasError = true
                        )
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun loadSeriesData(userId: Long) {
        viewModelScope.launch {
            seriesPointsRepository.getSeriesPoints(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val comps = resource.data.competitions
                        val groups = comps
                            .mapNotNull { comp ->
                                WeaponClassGroup.values().firstOrNull { it.matches(comp.weaponClass) }
                            }
                            .toSet()
                        val currentSelected = _uiState.value.selectedGroup
                        val newSelected = when {
                            currentSelected != null && currentSelected in groups -> currentSelected
                            else -> groups.firstOrNull()
                        }
                        _uiState.value = _uiState.value.copy(
                            competitions = comps,
                            allParticipants = resource.data.allParticipants,
                            availableGroups = groups,
                            selectedGroup = newSelected,
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
                        _uiState.value = _uiState.value.copy(isLoading = resource.isLoading)
                    }
                }
            }
        }
    }

    private fun loadClubMembers() {
        viewModelScope.launch {
            clubRepository.getUserClub().collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(
                        clubMembers = resource.data.club.users
                    )
                }
            }
        }
    }

    override fun selectShooter(userId: Long) {
        val name = _uiState.value.clubMembers
            .find { it.userId == userId }
            ?.let { it.fullname ?: "${it.name} ${it.lastname ?: ""}".trim() }
            ?: _uiState.value.allParticipants.find { it.userId == userId }?.fullname
            ?: return

        _uiState.value = _uiState.value.copy(
            selectedUserId = userId,
            selectedUserName = name,
            showSearchDialog = false,
            searchQuery = "",
            competitions = emptyList(),
            availableGroups = emptySet(),
            selectedGroup = null,
            isLoading = true
        )
        loadSeriesData(userId)
    }

    override fun selectWeaponGroup(group: WeaponClassGroup) {
        _uiState.value = _uiState.value.copy(selectedGroup = group)
    }

    override fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    override fun setShowSearchDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showSearchDialog = show,
            searchQuery = if (!show) "" else _uiState.value.searchQuery
        )
    }
}
