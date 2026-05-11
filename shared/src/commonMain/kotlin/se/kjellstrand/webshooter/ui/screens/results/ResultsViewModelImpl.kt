package se.kjellstrand.webshooter.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.StdMedal.B
import se.kjellstrand.webshooter.data.results.remote.StdMedal.S
import se.kjellstrand.webshooter.data.settings.SettingsRepository

open class ResultsViewModelImpl(
    override val competitionId: Long,
    override val competitionDate: String,
    private val resultsType: ResultsType,
    private val competitionName: String,
    private val resultsRepository: ResultsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel(), ResultsViewModel {

    private val _uiState = MutableStateFlow(ResultsUiState(isLoading = true, competitionName = competitionName))
    override val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private val _resultsEvent = Channel<ResultsEvent>(Channel.BUFFERED)
    override val resultsEvent = _resultsEvent.receiveAsFlow()

    init {
        getResults(competitionId)
        getLoggedInUserId()
    }

    private var myClubId: Long = -1L

    private fun getLoggedInUserId() {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collect { resource ->
                if (resource is Resource.Success) {
                    myClubId = resource.data.clubsId
                    _uiState.update { current ->
                        current.copy(
                            loggedInUserId = resource.data.userId,
                            groupedResults = groupResults(current.filterResults, resultsType, current.groupingMode, myClubId)
                        )
                    }
                }
            }
        }
    }

    private fun getResults(competitionId: Long) {
        viewModelScope.launch {
            resultsRepository.get(competitionId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        if (resource.data.results.isEmpty()) {
                            _uiState.update { it.copy(isLoading = false) }
                            _resultsEvent.send(ResultsEvent.Empty)
                        } else {
                            _uiState.update { current ->
                                current.copy(
                                    results = resource.data.results,
                                    filterResults = filterResults(resource.data.results, resultsType),
                                    groupedResults = groupResults(resource.data.results, resultsType, current.groupingMode, myClubId),
                                    allWeaponGroups = getWeaponGroups(resource.data.results).toList().sorted(),
                                    selectedWeaponGroups = getWeaponGroups(resource.data.results),
                                    isLoading = false,
                                    resultsType = resultsType,
                                    refreshVersion = current.refreshVersion + 1
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, refreshVersion = it.refreshVersion + 1) }
                    }

                    else -> {}
                }
            }
        }
    }

    override fun setMode(mode: Mode) {
        _uiState.update { currentState ->
            currentState.copy(mode = mode)
        }
    }

    override fun setSelectedWeaponGroups(selectedWeaponGroups: Set<String>) {
        _uiState.update { currentState ->
            currentState.copy(selectedWeaponGroups = selectedWeaponGroups)
        }

        val selectedGroups = _uiState.value.selectedWeaponGroups
        val filtered = if (selectedGroups.isEmpty()) {
            _uiState.value.results
        } else {
            _uiState.value.results.filter { result ->
                selectedGroups.contains(result.weaponClass.classname)
            }
        }.sortedByDescending { calculateSortOrder(it, resultsType) }

        val currentGroupingMode = _uiState.value.groupingMode
        _uiState.update { currentState ->
            currentState.copy(
                filterResults = filtered,
                groupedResults = groupResults(filtered, resultsType, currentGroupingMode, myClubId)
            )
        }
    }

    override fun refresh() {
        getResults(competitionId)
    }

    override fun setGroupingMode(groupingMode: GroupingMode) {
        val filtered = _uiState.value.filterResults
        _uiState.update { currentState ->
            currentState.copy(
                groupingMode = groupingMode,
                groupedResults = groupResults(filtered, resultsType, groupingMode, myClubId)
            )
        }
    }

    companion object {
        // Stable sentinel header values for missing club/medal. The screen
        // translates these to localized labels via stringResource() at render
        // time, keeping this VM free of any Android Context dependency.
        const val GROUP_HEADER_UNKNOWN_CLUB: String = "__webshooter_unknown_club__"
        const val GROUP_HEADER_NO_MEDAL: String = "__webshooter_no_medal__"

        fun groupResults(
            results: List<Result>,
            resultsType: ResultsType,
            groupingMode: GroupingMode = GroupingMode.WEAPON_CLASS,
            myClubId: Long = -1L
        ): List<GroupedItem> {
            return when (groupingMode) {
                GroupingMode.WEAPON_CLASS -> groupByWeaponClass(results, resultsType)
                GroupingMode.CLUB -> groupByClub(results, resultsType, myClubId)
                GroupingMode.MEDL -> groupByMedl(results, resultsType)
                GroupingMode.NONE -> emptyList()
            }
        }

        private fun groupByWeaponClass(results: List<Result>, resultsType: ResultsType): List<GroupedItem> {
            val weaponClasses = results.map { it.weaponClass.classname }.distinct().sorted()
            return weaponClasses.mapNotNull { weaponClass ->
                val grouped = results.filter { it.weaponClass.classname == weaponClass }
                    .sortedWith(compareBy { if (it.placement == 0L) Long.MAX_VALUE else it.placement })
                if (grouped.isNotEmpty()) GroupedItem(weaponClass, grouped) else null
            }
        }

        private fun groupByClub(results: List<Result>, resultsType: ResultsType, myClubId: Long = -1L): List<GroupedItem> {
            val clubIdByName = results.mapNotNull { it.signup.club }.associate { it.name to it.id }
            val clubs = results.map { it.signup.club?.name ?: GROUP_HEADER_UNKNOWN_CLUB }.distinct()
                .sortedWith(compareBy(
                    { if ((clubIdByName[it] ?: -1L) == myClubId) 0 else 1 },
                    { it }
                ))
            return clubs.mapNotNull { club ->
                val grouped = results.filter { (it.signup.club?.name ?: GROUP_HEADER_UNKNOWN_CLUB) == club }
                    .sortedByDescending { calculateSortOrder(it, resultsType) }
                if (grouped.isNotEmpty()) GroupedItem(club, grouped) else null
            }
        }

        private fun groupByMedl(results: List<Result>, resultsType: ResultsType): List<GroupedItem> {
            val medalOrder = { medal: String -> when (medal) { S.value -> 0; B.value -> 1; else -> 2 } }
            val medals = results.map { it.stdMedal?.value ?: GROUP_HEADER_NO_MEDAL }.distinct()
                .sortedWith(compareBy({ medalOrder(it) }, { it }))
            return medals.mapNotNull { medal ->
                val grouped = results.filter { (it.stdMedal?.value ?: GROUP_HEADER_NO_MEDAL) == medal }
                    .sortedByDescending { calculateSortOrder(it, resultsType) }
                if (grouped.isNotEmpty()) GroupedItem(medal, grouped) else null
            }
        }

        fun filterResults(results: List<Result>, resultsType: ResultsType): List<Result> {
            val weaponClasses = getWeaponGroups(results)
            return results.filter {
                it.weaponClass.classname in weaponClasses
            }.sortedByDescending { calculateSortOrder(it, resultsType) }
        }

        fun getWeaponGroups(results: List<Result>): Set<String> {
            return results.map { it.weaponClass.classname }.toSet()
        }

        fun calculateSortOrder(result: Result, resultsType: ResultsType): Int {
            return when (resultsType) {
                ResultsType.MILITARY,
                ResultsType.PRECISION -> (result.points * 1000 + result.hits).toInt()
                ResultsType.FIELD -> (result.hits * 1000000 + result.figureHits * 1000 + result.points).toInt()
                ResultsType.POINTS_FIELD -> (result.hits + result.figureHits).toInt()
            }
        }
    }
}
