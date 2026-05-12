package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards that every Compose screen acquires its ViewModel through Koin
 * (`koinViewModel<...>()`) instead of constructing one directly. After the
 * KMP migration, `hiltViewModel()` is gone — the equivalent invariant is
 * "uses koinViewModel with an explicit type parameter."
 *
 * The 3 SavedStateHandle screens (TeamsScreen, SignupsScreen, PatrolsScreen)
 * receive their VM as a required parameter from AppNavHost.kt — so they
 * don't acquire it themselves. They're excluded from this scan.
 */
class ViewModelInjectionTest {

    private val screenFiles = listOf(
        "club/ClubScreen.kt",
        "splash/SplashScreen.kt",
        "settings/SettingsScreen.kt",
        "login/LoginScreen.kt",
        "myresults/MyResultsScreen.kt",
        "competitions/CompetitionsScreen.kt"
    ).map { File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/$it") }

    @Test
    fun `all screen composables use explicit koinViewModel type parameter`() {
        val violations = mutableListOf<String>()
        screenFiles.forEach { file ->
            if (file.exists()) {
                val source = file.readText()
                // Match "= koinViewModel()" without a type parameter
                val hasImplicit = Regex("""= koinViewModel\(\)\s*[,\n)\{]""").containsMatchIn(source)
                if (hasImplicit) {
                    violations.add(file.name)
                }
            }
        }
        assertTrue(
            "These screens use koinViewModel() without explicit type parameter: $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun `ClubScreen uses explicit koinViewModel type`() {
        val source = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/club/ClubScreen.kt").readText()
        assertTrue(
            "ClubScreen should use koinViewModel<ClubViewModelImpl>()",
            source.contains("koinViewModel<ClubViewModelImpl>()")
        )
    }

    @Test
    fun `all screen files exist`() {
        screenFiles.forEach { file ->
            assertTrue("${file.name} should exist", file.exists())
        }
    }

    @Test
    fun `all screens use koinViewModel`() {
        screenFiles.forEach { file ->
            val source = file.readText()
            assertTrue(
                "${file.name} should use koinViewModel",
                source.contains("koinViewModel")
            )
        }
    }
}
