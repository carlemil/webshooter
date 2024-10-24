package se.kjellstrand.webshooter.ui.navigation

sealed class Screen(val route: String) {
    data object LoginScreen : Screen("login_screen")
    data object LandingScreen : Screen("landing_screen")
    data object CompetitionsList : Screen("competitions_list")
    data object CompetitionDetail : Screen("competition_detail/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_detail/$competitionId"
    }
    data object CompetitionResults : Screen("competition_results/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_results/$competitionId"
    }
}