package se.kjellstrand.webshooter.ui

import se.kjellstrand.webshooter.data.competitions.remote.Competitions

data class CompetitionsUiState(
    val competitions: Competitions? = null
)