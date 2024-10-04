package se.kjellstrand.webshooter.ui.results

import se.kjellstrand.webshooter.data.results.remote.Result

data class ResultsUiState(
    val results: List<Result> = listOf(),
    val groupedResults: List<GroupedItem> = listOf(),
    var mode: Mode = Mode.GROUP
)

data class GroupedItem(
    val header: String,
    val items: List<Result>
)

data class FilterState(
    val option1: Boolean = false,
    val option2: Boolean = false
    // Add more options as needed
)

enum class Mode {
    GROUP,
    FILTER
}
