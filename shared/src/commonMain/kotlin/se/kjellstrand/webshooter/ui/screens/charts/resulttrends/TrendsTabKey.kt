package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

import se.kjellstrand.webshooter.data.competitions.remote.ResultsType

const val MAGNUMPRECISION_TAB_KEY = "magnumprecision"
const val MAGNUMPRECISION_COMPETITION_TYPE_NAME = "Magnumprecision"

fun trendsTabKeyFor(competitionTypeName: String?, resultsType: String): String =
    if (competitionTypeName == MAGNUMPRECISION_COMPETITION_TYPE_NAME) MAGNUMPRECISION_TAB_KEY
    else resultsType

fun trendsTabDisplayName(tabKey: String): String = when (tabKey) {
    MAGNUMPRECISION_TAB_KEY -> "M.-prec"
    else -> ResultsType.fromApiString(tabKey)?.displayName
        ?: tabKey.replaceFirstChar { it.uppercase() }
}
