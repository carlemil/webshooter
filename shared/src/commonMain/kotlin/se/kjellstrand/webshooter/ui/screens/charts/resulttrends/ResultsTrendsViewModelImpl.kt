package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.charts.ChartsRepository
import se.kjellstrand.webshooter.data.charts.CompetitionMeta
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import se.kjellstrand.webshooter.data.clubstats.WeaponClassGroup

class ResultsTrendsViewModelImpl(
    private val chartsRepository: ChartsRepository,
    private val settingsRepository: SettingsRepository,
    private val clubRepository: ClubRepository,
) : ViewModel(), ResultsTrendsViewModel {

    private val _uiState = MutableStateFlow(ChartsUiState(isLoading = true))
    override val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()

    private var currentUserId: Long = 0L
    private var allCompetitionMeta: Map<Long, CompetitionMeta> = emptyMap()

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
                        _uiState.value = _uiState.value.copy(currentUserId = currentUserId)
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
                        allCompetitionMeta = resource.data.allCompetitionMeta
                        val grouped = resource.data.dataPoints.groupBy {
                            trendsTabKeyFor(it.competitionTypeName, it.resultsType)
                        }
                        // Derive available tabs from metadata so they appear as
                        // soon as the repository emits its initial metadata-only
                        // Success — before any datapoints have streamed in.
                        val hiddenTypes = setOf("pointfield")
                        val systemTypes = resource.data.allCompetitionMeta.values
                            .map { trendsTabKeyFor(it.competitionTypeName, it.resultsType) }
                            .distinct()
                            .filter { it !in hiddenTypes }
                        val availableTypes = (systemTypes + listOfNotNull(
                            MAGNUMPRECISION_TAB_KEY.takeIf { grouped.containsKey(MAGNUMPRECISION_TAB_KEY) }
                        )).distinct().sorted()
                        val selectedType = _uiState.value.selectedResultsType.ifEmpty {
                            availableTypes.firstOrNull() ?: ""
                        }
                        val effectiveGroups = groupsForTab(selectedType)
                        val currentGroup = _uiState.value.selectedGroup
                        val newGroup = if (currentGroup != null && currentGroup in effectiveGroups) currentGroup
                                       else defaultGroupForTab(selectedType)

                        _uiState.value = _uiState.value.copy(
                            chartData = grouped,
                            availableResultsTypes = availableTypes,
                            availableGroups = effectiveGroups,
                            selectedGroup = newGroup,
                            allParticipants = resource.data.allParticipants,
                            selectedResultsType = selectedType,
                            hasError = false
                        )
                        loadAllParticipantsPoints(resource.data.allParticipants.map { it.userId })
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

    private fun loadAllParticipantsPoints(participantIds: List<Long>) {
        if (participantIds.isEmpty()) return
        val competitionIds = allCompetitionMeta.keys.toList()
        if (competitionIds.isEmpty()) return
        val metadata = allCompetitionMeta
        viewModelScope.launch {
            chartsRepository.getShooterChartData(
                shooterIds = participantIds,
                competitionIds = competitionIds,
                competitionMetadata = metadata
            ).collect { resource ->
                if (resource is Resource.Success) {
                    val points = resource.data.mapValues { (_, data) -> data.dataPoints }
                    _uiState.value = _uiState.value.copy(allParticipantsPoints = points)
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
        val groups = groupsForTab(resultsType)
        _uiState.value = _uiState.value.copy(
            selectedResultsType = resultsType,
            availableGroups = groups,
            selectedGroup = defaultGroupForTab(resultsType)
        )
    }

    override fun selectWeaponGroup(group: WeaponClassGroup?) {
        if (_uiState.value.selectedGroup == group) return
        _uiState.value = _uiState.value.copy(selectedGroup = group)
    }

    override fun addShooter(userId: Long) {
        if (userId == currentUserId) return
        if (_uiState.value.comparedShooters.containsKey(userId)) return

        val shooterName = _uiState.value.clubMembers
            .find { it.userId == userId }
            ?.let { it.fullname ?: "${it.name} ${it.lastname ?: ""}".trim() }
            ?: _uiState.value.allParticipants
                .find { it.userId == userId }?.fullname
            ?: return

        // Add immediately with empty data so the dialog reflects the selection
        // right away. Chart data is backfilled asynchronously below.
        _uiState.value = _uiState.value.copy(
            comparedShooters = _uiState.value.comparedShooters + (userId to ShooterChartInfo(
                name = shooterName,
                chartData = emptyList()
            ))
        )

        val competitionIds = allCompetitionMeta.keys.toList()
        val competitionMetadata = allCompetitionMeta

        viewModelScope.launch {
            chartsRepository.getShooterChartData(
                shooterIds = listOf(userId),
                competitionIds = competitionIds,
                competitionMetadata = competitionMetadata
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

    override fun refresh() {
        if (currentUserId == 0L) return
        loadChartData(currentUserId)
    }
}
