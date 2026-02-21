package se.kjellstrand.webshooter.ui.navigation

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login")
    object LandingScreen : Screen("landing")
    object CompetitionsList : Screen("competitions")
    object CompetitionDetail : Screen("competition_detail/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_detail/$competitionId"
    }
    object CompetitionResults : Screen("competition_results/{competitionId}/{resultsType}") {
        fun createRoute(competitionId: Int, resultsType: String) =
            "competition_results/$competitionId/$resultsType"
    }
    object MyEntries : Screen("my_entries")
    object Settings : Screen("settings")
    object ShooterResult : Screen("shooter_result/{competitionId}/{shooterId}/{resultsType}") {
        fun createRoute(competitionId: Int, shooterId: Int, resultsType: String) =
            "shooter_result/$competitionId/$shooterId/$resultsType"
    }
}