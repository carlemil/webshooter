package se.kjellstrand.webshooter.ui.screens.results

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.StdMedal.B
import se.kjellstrand.webshooter.data.results.remote.StdMedal.S
import se.kjellstrand.webshooter.data.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
open class ResultsViewModelImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resultsRepository: ResultsRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ResultsViewModel {

    override val competitionId: Long = checkNotNull(savedStateHandle["competitionId"])
    override val competitionDate: String = savedStateHandle["competitionDate"] ?: ""

    private val resultsType: ResultsType = try {
        ResultsType.valueOf(checkNotNull(savedStateHandle["resultsType"]))
    } catch (e: Exception) {
        ResultsType.FIELD
    }

    private val competitionName: String = savedStateHandle["competitionName"] ?: ""

    private val _uiState = MutableStateFlow(ResultsUiState(isLoading = true, competitionName = competitionName))
    override val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private val _resultsEvent = MutableSharedFlow<ResultsEvent>()
    override val resultsEvent: SharedFlow<ResultsEvent> = _resultsEvent.asSharedFlow()

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
                            groupedResults = groupResults(current.filterResults, resultsType, current.groupingMode, context, myClubId)
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
                            _resultsEvent.emit(ResultsEvent.Empty)
                        } else {
                            _uiState.update { current ->
                                current.copy(
                                    results = resource.data.results,
                                    filterResults = filterResults(resource.data.results, resultsType),
                                    groupedResults = groupResults(resource.data.results, resultsType, current.groupingMode, context, myClubId),
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

                    else -> {
                        println("getResults is loading competitionId: $competitionId")
                    }
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
                groupedResults = groupResults(filtered, resultsType, currentGroupingMode, context, myClubId)
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
                groupedResults = groupResults(filtered, resultsType, groupingMode, context, myClubId)
            )
        }
    }

    companion object {
        fun groupResults(
            results: List<Result>,
            resultsType: ResultsType,
            groupingMode: GroupingMode = GroupingMode.WEAPON_CLASS,
            context: Context? = null,
            myClubId: Long = -1L
        ): List<GroupedItem> {
            return when (groupingMode) {
                GroupingMode.WEAPON_CLASS -> groupByWeaponClass(results, resultsType)
                GroupingMode.CLUB -> groupByClub(results, resultsType, context, myClubId)
                GroupingMode.MEDL -> groupByMedl(results, resultsType, context)
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

        private fun groupByClub(results: List<Result>, resultsType: ResultsType, context: Context?, myClubId: Long = -1L): List<GroupedItem> {
            val unknown = context?.getString(R.string.unknown) ?: "Unknown"
            val clubIdByName = results.mapNotNull { it.signup.club }.associate { it.name to it.id }
            val clubs = results.map { it.signup.club?.name ?: unknown }.distinct()
                .sortedWith(compareBy(
                    { if ((clubIdByName[it] ?: -1L) == myClubId) 0 else 1 },
                    { it }
                ))
            return clubs.mapNotNull { club ->
                val grouped = results.filter { (it.signup.club?.name ?: unknown) == club }
                    .sortedByDescending { calculateSortOrder(it, resultsType) }
                if (grouped.isNotEmpty()) GroupedItem(club, grouped) else null
            }
        }

        private fun groupByMedl(results: List<Result>, resultsType: ResultsType, context: Context?): List<GroupedItem> {
            val dash = context?.getString(R.string.dash) ?: "-"
            val medalOrder = { medal: String -> when (medal) { S.value -> 0; B.value -> 1; else -> 2 } }
            val medals = results.map { it.stdMedal?.value ?: dash }.distinct()
                .sortedWith(compareBy({ medalOrder(it) }, { it }))
            return medals.mapNotNull { medal ->
                val grouped = results.filter { (it.stdMedal?.value ?: dash) == medal }
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
