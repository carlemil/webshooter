package se.kjellstrand.webshooter.ui.navigation

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login")
    object LandingScreen : Screen("landing")
    object CompetitionsList : Screen("competitions")
    object CompetitionResults : Screen("competition_results/{competitionId}/{resultsType}?competitionName={competitionName}") {
        fun createRoute(competitionId: Int, resultsType: String, competitionName: String) =
            "competition_results/$competitionId/$resultsType?competitionName=${android.net.Uri.encode(competitionName)}"
    }
    object Club : Screen("club")
    object MyEntries : Screen("my_entries")
    object Settings : Screen("settings")
    object ShooterResult : Screen("shooter_result/{competitionId}/{shooterId}/{resultsType}") {
        fun createRoute(competitionId: Int, shooterId: Int, resultsType: String) =
            "shooter_result/$competitionId/$shooterId/$resultsType"
    }
    object CompetitionSignup : Screen("competition_signup/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_signup/$competitionId"
    }
}