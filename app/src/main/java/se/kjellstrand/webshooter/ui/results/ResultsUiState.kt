package se.kjellstrand.webshooter.ui.results

import se.kjellstrand.webshooter.data.results.remote.Result

data class ResultsUiState(
    val results: List<Result> = listOf(),
    val groupedResults: List<GroupedItem> = listOf(),
    var layoutType: LayoutType = LayoutType.GROUP
)

enum class LayoutType {
    GROUP,
    FILTER
}