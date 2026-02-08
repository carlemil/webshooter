package se.kjellstrand.webshooter.ui.navigation

sealed class Screen(val route: String) {
    data object LoginScreen : Screen("login")
    data object LandingScreen : Screen("landing")
    data object CompetitionsList : Screen("competitions")
    data object CompetitionDetail : Screen("competition_detail/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_detail/$competitionId"
    }
    data object CompetitionResults : Screen("competition_results/{competitionId}") {
        fun createRoute(competitionId: Int) = "competition_results/$competitionId"
    }
}
