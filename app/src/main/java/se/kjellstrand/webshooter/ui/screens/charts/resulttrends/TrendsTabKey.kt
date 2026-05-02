package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

const val MAGNUMPRECISION_TAB_KEY = "magnumprecision"
const val MAGNUMPRECISION_COMPETITION_TYPE_NAME = "Magnumprecision"

fun trendsTabKeyFor(competitionTypeName: String?, resultsType: String): String =
    if (competitionTypeName == MAGNUMPRECISION_COMPETITION_TYPE_NAME) MAGNUMPRECISION_TAB_KEY
    else resultsType
