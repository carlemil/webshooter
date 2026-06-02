package se.kjellstrand.webshooter.ui.navigation

import se.kjellstrand.webshooter.data.competitions.remote.ResultsType

/**
 * Maps an inbound deep-link URL (Universal Link or `webshooter://` scheme)
 * to an internal Compose `Screen.<x>.createRoute(...)` string.
 *
 * Returns `null` for URLs we don't recognise so the caller can fall back to
 * doing nothing (better than navigating somewhere wrong).
 *
 * Supported patterns (mirrors `navDeepLink` registrations in [AppNavHost]):
 *   * https://webshooter.se/app/competition_results/{competitionId}/{resultsType}
 *   * https://webshooter.se/app/shooter_result/{competitionId}/{shooterId}/{resultsType}
 *   * https://webshooter.se/app/competition_signup/{competitionId}
 *   * webshooter:///competition_results/{competitionId}/{resultsType}
 *   * webshooter:///shooter_result/{competitionId}/{shooterId}/{resultsType}
 *   * webshooter:///competition_signup/{competitionId}
 */
object DeepLinkRouter {

    private const val UNIVERSAL_PREFIX = "${Screen.DEEP_LINK_BASE_URI}/"
    private const val SCHEME_PREFIX = "webshooter:///"

    fun resolve(rawUrl: String): String? {
        val tail = when {
            rawUrl.startsWith(UNIVERSAL_PREFIX) -> rawUrl.removePrefix(UNIVERSAL_PREFIX)
            rawUrl.startsWith(SCHEME_PREFIX) -> rawUrl.removePrefix(SCHEME_PREFIX)
            else -> return null
        }
        val parts = tail.substringBefore('?').split('/').filter { it.isNotEmpty() }
        return when (parts.firstOrNull()) {
            "competition_results" -> resolveCompetitionResults(parts)
            "shooter_result" -> resolveShooterResult(parts)
            "competition_signup" -> resolveCompetitionSignup(parts)
            else -> null
        }
    }

    private fun resolveCompetitionResults(parts: List<String>): String? {
        if (parts.size < 3) return null
        val competitionId = parts[1].toLongOrNull() ?: return null
        val resultsType = parts[2].uppercase()
        // Validate resultsType against the enum to avoid silent garbage.
        runCatching { ResultsType.valueOf(resultsType) }.getOrElse { return null }
        return Screen.CompetitionResults.createRoute(
            competitionId = competitionId,
            resultsType = resultsType,
            competitionName = "",
            competitionDate = "",
        )
    }

    private fun resolveShooterResult(parts: List<String>): String? {
        if (parts.size < 4) return null
        val competitionId = parts[1].toLongOrNull() ?: return null
        val shooterId = parts[2].toLongOrNull() ?: return null
        val resultsType = parts[3].uppercase()
        runCatching { ResultsType.valueOf(resultsType) }.getOrElse { return null }
        return Screen.ShooterResult.createRoute(competitionId, shooterId, resultsType)
    }

    private fun resolveCompetitionSignup(parts: List<String>): String? {
        if (parts.size < 2) return null
        val competitionId = parts[1].toLongOrNull() ?: return null
        return Screen.CompetitionSignup.createRoute(competitionId)
    }
}
