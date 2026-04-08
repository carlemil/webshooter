package se.kjellstrand.webshooter.ui.navigation

sealed class Screen(val route: String) {

    companion object {
        const val DEEP_LINK_BASE_URI = "https://webshooter.se/app"
    }

    object SplashScreen : Screen("splash")
    object LoginScreen : Screen("login")
    object LandingScreen : Screen("landing")
    object CompetitionsList : Screen("competitions")
    object CompetitionResults : Screen("competition_results/{competitionId}/{resultsType}?competitionName={competitionName}&competitionDate={competitionDate}") {
        const val deepLink = "$DEEP_LINK_BASE_URI/competition_results/{competitionId}/{resultsType}"
        fun createRoute(competitionId: Long, resultsType: String, competitionName: String, competitionDate: String) =
            "competition_results/$competitionId/$resultsType?competitionName=${android.net.Uri.encode(competitionName)}&competitionDate=${android.net.Uri.encode(competitionDate)}"
    }
    object Club : Screen("club")
    object MyEntries : Screen("my_entries")
    object Charts : Screen("charts")
    object Settings : Screen("settings")
    object Licenses : Screen("licenses")
    object ShooterResult : Screen("shooter_result/{competitionId}/{shooterId}/{resultsType}") {
        const val deepLink = "$DEEP_LINK_BASE_URI/shooter_result/{competitionId}/{shooterId}/{resultsType}"
        fun createRoute(competitionId: Long, shooterId: Long, resultsType: String) =
            "shooter_result/$competitionId/$shooterId/$resultsType"
    }
    object CompetitionSignup : Screen("competition_signup/{competitionId}") {
        const val deepLink = "$DEEP_LINK_BASE_URI/competition_signup/{competitionId}"
        fun createRoute(competitionId: Long) = "competition_signup/$competitionId"
    }
    object CompetitionSignupsList : Screen("competition_signups_list/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_signups_list/$competitionId"
    }
    object CompetitionPatrols : Screen("competition_patrols/{competitionId}/{competitionTypeId}") {
        fun createRoute(competitionId: Long, competitionTypeId: Int) =
            "competition_patrols/$competitionId/$competitionTypeId"
    }
    object CompetitionTeams : Screen("competition_teams/{competitionId}") {
        fun createRoute(competitionId: Long) = "competition_teams/$competitionId"
    }
}