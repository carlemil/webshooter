package se.kjellstrand.webshooter.ui

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object LandingScreen : Screen("landing_screen")
    object CompetitionsList : Screen("competitions_list")
    object CompetitionDetail : Screen("competition_detail/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_detail/$competitionId"
    }
}