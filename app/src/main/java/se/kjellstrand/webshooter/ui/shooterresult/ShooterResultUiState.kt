package se.kjellstrand.webshooter.ui.shooterresult

import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.results.GroupedItem

data class ShooterResultUiState(
    val isLoading: Boolean = true,
    val shooterName: String = "",
    val results: List<Result> = emptyList(),
    val groupedResults: List<GroupedItem> = emptyList(),
    val error: String? = null,
    val resultsType: ResultsType = ResultsType.FIELD
)