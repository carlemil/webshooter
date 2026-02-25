package se.kjellstrand.webshooter.ui.results

import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.remote.Result

data class ResultsUiState(
    val results: List<Result> = listOf(),
    val filterResults: List<Result> = listOf(),
    val groupedResults: List<GroupedItem> = listOf(),
    val allWeaponGroups: List<String> = listOf(),
    val selectedWeaponGroups: Set<String> = emptySet(),
    var mode: Mode = Mode.GROUP,
    val isLoading: Boolean = false,
    val resultsType: ResultsType = ResultsType.FIELD,
    val loggedInUserId: Long = -1L,
    val groupingMode: GroupingMode = GroupingMode.WEAPON_CLASS
)

data class GroupedItem(
    val header: String,
    val items: List<Result>
)

data class FilterState(
    val selectedWeaponGroups: Set<String> = emptySet(),
    val groupingMode: GroupingMode = GroupingMode.WEAPON_CLASS
)

enum class Mode {
    GROUP,
    FILTER
}

enum class GroupingMode {
    WEAPON_CLASS,
    CLUB,
    MEDL,
    NONE
}
