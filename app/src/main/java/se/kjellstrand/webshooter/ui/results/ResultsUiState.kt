package se.kjellstrand.webshooter.ui.results

import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.common.GroupedItem

data class ResultsUiState(
    val results: List<Result>? = null,
    val groupedResults: List<GroupedItem>? = null,
    val showClassname: Boolean = false
)