package se.kjellstrand.webshooter.ui.screens.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.charts.ChartData
import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import se.kjellstrand.webshooter.data.charts.ChartsRepository
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class ChartsViewModelImpl @Inject constructor(
    private val chartsRepository: ChartsRepository,
    private val settingsRepository: SettingsRepository,
    private val clubRepository: ClubRepository
) : ViewModel(), ChartsViewModel {

    private val _uiState = MutableStateFlow(ChartsUiState(isLoading = true))
    override val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()

    private var currentUserId: Long = 0L
    private var allChartDataPoints: List<ChartDataPoint> = emptyList()

    init {
        loadUserAndChartData()
        loadClubMembers()
    }

    private fun loadUserAndChartData() {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        currentUserId = resource.data.userId
                        loadChartData(currentUserId)
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

    private fun loadChartData(userId: Long) {
        viewModelScope.launch {
            chartsRepository.getChartData(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        allChartDataPoints = resource.data.dataPoints
                        val grouped = resource.data.dataPoints.groupBy { it.resultsType }
                        val availableTypes = grouped.keys.toList().sorted()
                        val availableClasses = resource.data.dataPoints
                            .map { it.weaponClass }
                            .distinct()
                            .sorted()
                        val selectedType = _uiState.value.selectedResultsType.ifEmpty {
                            availableTypes.firstOrNull() ?: ""
                        }

                        _uiState.value = _uiState.value.copy(
                            chartData = grouped,
                            availableResultsTypes = availableTypes,
                            availableWeaponClasses = availableClasses,
                            selectedResultsType = selectedType,
                            isLoading = false,
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

    private fun loadClubMembers() {
        viewModelScope.launch {
            clubRepository.getUserClub().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            clubMembers = resource.data.club.users
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    override fun selectTab(resultsType: String) {
        _uiState.value = _uiState.value.copy(selectedResultsType = resultsType)
    }

    override fun toggleWeaponClass(weaponClass: String) {
        val current = _uiState.value.selectedWeaponClasses
        val updated = if (weaponClass in current) {
            current - weaponClass
        } else {
            current + weaponClass
        }
        _uiState.value = _uiState.value.copy(selectedWeaponClasses = updated)
    }

    override fun addShooter(userId: Long) {
        if (userId == currentUserId) return
        if (_uiState.value.comparedShooters.containsKey(userId)) return

        val member = _uiState.value.clubMembers.find { it.userId == userId } ?: return
        val shooterName = member.fullname ?: "${member.name} ${member.lastname ?: ""}".trim()

        val competitionIds = allChartDataPoints.map { it.competitionId }.distinct()

        viewModelScope.launch {
            chartsRepository.getShooterChartData(
                shooterIds = listOf(userId),
                competitionIds = competitionIds
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val shooterData = resource.data[userId]
                        if (shooterData != null) {
                            val updated = _uiState.value.comparedShooters + (userId to ShooterChartInfo(
                                name = shooterName,
                                chartData = shooterData.dataPoints
                            ))
                            _uiState.value = _uiState.value.copy(comparedShooters = updated)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun removeShooter(userId: Long) {
        _uiState.value = _uiState.value.copy(
            comparedShooters = _uiState.value.comparedShooters - userId
        )
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
