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
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class ClubViewModel @Inject constructor(
    private val clubRepository: ClubRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClubUiState(isLoading = true))
    val uiState: StateFlow<ClubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collect { resource ->
                if (resource is Resource.Success) {
                    val clubId = resource.data.clubsId
                    loadAll(clubId)
                }
            }
        }
    }

    private fun loadAll(clubId: Long) {
        viewModelScope.launch {
            clubRepository.getClubInfo(clubId).collect { resource ->
                when (resource) {
                    is Resource.Success -> _uiState.update { it.copy(clubInfo = resource.data.club, isLoading = false) }
                    is Resource.Error -> _uiState.update { it.copy(error = resource.error.name, isLoading = false) }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = resource.isLoading) }
                }
            }
        }
        viewModelScope.launch {
            clubRepository.getClubAdmins(clubId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(admins = resource.data.admins) }
                }
            }
        }
        viewModelScope.launch {
            clubRepository.getClubUsers(clubId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(users = resource.data.users) }
                }
            }
        }
    }

    fun selectTab(tab: ClubTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
