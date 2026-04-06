package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ViewModelInjectionTest {

    private val screenFiles = listOf(
        "club/ClubScreen.kt",
        "splash/SplashScreen.kt",
        "shooterresult/ShooterResultScreen.kt",
        "settings/SettingsScreen.kt",
        "login/LoginScreen.kt"
    ).map { File("src/main/java/se/kjellstrand/webshooter/ui/screens/$it") }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `all screen composables use explicit hiltViewModel type parameter`() {
        val violations = mutableListOf<String>()
        screenFiles.forEach { file ->
            if (file.exists()) {
                val source = file.readText()
                // Match "= hiltViewModel()" without a type parameter
                val hasImplicit = Regex("""= hiltViewModel\(\)\s*[,\n)\{]""").containsMatchIn(source)
                if (hasImplicit) {
                    violations.add(file.name)
                }
            }
        }
        assertTrue(
            "These screens use hiltViewModel() without explicit type parameter: $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun `ClubScreen uses explicit hiltViewModel type`() {
        val source = File("src/main/java/se/kjellstrand/webshooter/ui/screens/club/ClubScreen.kt").readText()
        assertTrue(
            "ClubScreen should use hiltViewModel<ClubViewModelImpl>()",
            source.contains("hiltViewModel<ClubViewModelImpl>()")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `all screen files exist`() {
        screenFiles.forEach { file ->
            assertTrue("${file.name} should exist", file.exists())
        }
    }

    @Test
    fun `screens with Impl classes already use explicit type`() {
        val teamsScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/teams/TeamsScreen.kt")
        val source = teamsScreen.readText()
        assertTrue(
            "TeamsScreen should use hiltViewModel<TeamsViewModelImpl>()",
            source.contains("hiltViewModel<TeamsViewModelImpl>()")
        )
    }

    @Test
    fun `all screens use hiltViewModel`() {
        screenFiles.forEach { file ->
            val source = file.readText()
            assertTrue(
                "${file.name} should use hiltViewModel",
                source.contains("hiltViewModel")
            )
        }
    }
}
