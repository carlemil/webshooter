package se.kjellstrand.webshooter.data.competitions

import kotlinx.serialization.json.Json
import org.junit.Test
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import se.kjellstrand.webshooter.data.competitionpatrols.remote.CompetitionPatrolsResponse
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupsResponse
import se.kjellstrand.webshooter.data.competitionteams.remote.CompetitionTeamsResponse
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse
import java.io.File

class MockJsonParseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Test
    fun parses_mock_competitions_txt() {
        val text = File("src/main/res/raw/competitions.txt").readText()
        println("competitions.txt size = ${text.length}")
        try {
            val parsed = json.decodeFromString<CompetitionsResponse>(text)
            println("OK: parsed ${parsed.competitions.data.size} entries; envelope status=${parsed.competitions.status}")
        } catch (e: Throwable) {
            println("PARSE FAILED: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun parses_mock_myentries_txt() {
        val text = File("src/main/res/raw/myentries.txt").readText()
        println("myentries.txt size = ${text.length}")
        try {
            val parsed = json.decodeFromString<CompetitionsResponse>(text)
            println("OK: parsed ${parsed.competitions.data.size} entries")
        } catch (e: Throwable) {
            println("PARSE FAILED: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun parses_all_per_competition_files() {
        val rawDir = File("src/main/res/raw")
        val failures = mutableListOf<String>()
        val checks = listOf(
            "results_" to { text: String -> json.decodeFromString<ResultsResponse>(text) },
            "signups_" to { text: String -> json.decodeFromString<CompetitionSignupsResponse>(text) },
            "patrols_" to { text: String -> json.decodeFromString<CompetitionPatrolsResponse>(text) },
            "teams_"   to { text: String -> json.decodeFromString<CompetitionTeamsResponse>(text) },
        )
        for ((prefix, decode) in checks) {
            val files = rawDir.listFiles { f -> f.name.startsWith(prefix) && f.name.endsWith(".txt") } ?: continue
            for (f in files.sortedBy { it.name }) {
                try {
                    decode(f.readText())
                } catch (e: Throwable) {
                    failures += "${f.name}: ${e::class.simpleName}: ${e.message?.lines()?.firstOrNull()}"
                }
            }
        }
        if (failures.isNotEmpty()) {
            println("PARSE FAILURES (${failures.size}):")
            failures.forEach { println("  $it") }
            throw AssertionError("${failures.size} per-competition mock(s) failed to parse")
        } else {
            println("OK: all per-competition mocks parse")
        }
    }
}
