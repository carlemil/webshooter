package se.kjellstrand.webshooter.ui.competitions

import se.kjellstrand.webshooter.data.competitions.remote.Competitions

data class CompetitionsUiState(
    val competitions: Competitions? = null
)