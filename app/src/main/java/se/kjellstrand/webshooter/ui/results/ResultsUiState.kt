package se.kjellstrand.webshooter.ui.results

import se.kjellstrand.webshooter.data.results.remote.Result

data class ResultsUiState(
    val results: List<Result> = listOf(),
    val filterResults: List<Result> = listOf(),
    val groupedResults: List<GroupedItem> = listOf(),
    val allWeaponGroups: List<String> = listOf(),
    val selectedWeaponGroups: Set<String> = emptySet(),
    var mode: Mode = Mode.GROUP
)

data class GroupedItem(
    val header: String,
    val items: List<Result>
)

data class FilterState(
    val selectedWeaponGroups: Set<String> = emptySet()
)

enum class Mode {
    GROUP,
    FILTER
}
