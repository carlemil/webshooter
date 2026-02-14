package se.kjellstrand.webshooter.ui.navigation

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login")
    object LandingScreen : Screen("landing")
    object CompetitionsList : Screen("competitions")
    object CompetitionDetail : Screen("competition_detail/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_detail/$competitionId"
    }
    object CompetitionResults : Screen("competition_results/{competitionId}") {
        fun createRoute(competitionId: Int) = "competition_results/$competitionId"
    }
    object ShooterResult : Screen("shooter_result/{competitionId}/{shooterId}") {
        fun createRoute(competitionId: Int, shooterId: Int) = "shooter_result/$competitionId/$shooterId"
    }
}