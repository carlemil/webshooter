package se.kjellstrand.webshooter.ui.screens.clubstats

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ClubStatsViewModelTest {

    private val baseDir = "src/main/java/se/kjellstrand/webshooter/ui/screens/clubstats"
    private val interfaceFile = File("$baseDir/ClubStatsViewModel.kt")
    private val implFile = File("$baseDir/ClubStatsViewModelImpl.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ClubStatsViewModel interface file exists`() {
        assertTrue(
            "ClubStatsViewModel.kt must exist",
            interfaceFile.exists()
        )
    }

    @Test
    fun `ClubStatsViewModel declares uiState StateFlow`() {
        val source = interfaceFile.readText()
        assertTrue(
            "ClubStatsViewModel must declare val uiState: StateFlow<ClubStatsUiState>",
            source.contains("val uiState: StateFlow<ClubStatsUiState>")
        )
    }

    @Test
    fun `ClubStatsViewModelImpl file exists`() {
        assertTrue(
            "ClubStatsViewModelImpl.kt must exist",
            implFile.exists()
        )
    }

    @Test
    fun `ClubStatsViewModelImpl is annotated with HiltViewModel`() {
        val source = implFile.readText()
        assertTrue(
            "ClubStatsViewModelImpl must be annotated with @HiltViewModel",
            source.contains("@HiltViewModel")
        )
    }

    @Test
    fun `ClubStatsViewModelImpl extends ViewModel and implements ClubStatsViewModel`() {
        val source = implFile.readText()
        val pattern = Regex("""class\s+ClubStatsViewModelImpl[\s\S]*:\s*ViewModel\(\)\s*,\s*ClubStatsViewModel""")
        assertTrue(
            "ClubStatsViewModelImpl must extend ViewModel() and implement ClubStatsViewModel",
            pattern.containsMatchIn(source)
        )
    }

    @Test
    fun `ClubStatsViewModelImpl depends on ClubStatsRepository`() {
        val source = implFile.readText()
        assertTrue(
            "ClubStatsViewModelImpl must depend on ClubStatsRepository",
            source.contains("ClubStatsRepository")
        )
    }

    @Test
    fun `ClubStatsViewModelImpl has MutableStateFlow for uiState`() {
        val source = implFile.readText()
        assertTrue(
            "ClubStatsViewModelImpl must use MutableStateFlow for _uiState",
            source.contains("MutableStateFlow(ClubStatsUiState(")
        )
    }

    @Test
    fun `ClubStatsViewModelImpl calls getClubStats in init`() {
        val source = implFile.readText()
        assertTrue(
            "ClubStatsViewModelImpl must call getClubStats(...)",
            source.contains("clubStatsRepository.getClubStats(")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ClubStatsUiState still exists`() {
        assertTrue(
            File("$baseDir/ClubStatsUiState.kt").exists()
        )
    }

    @Test
    fun `ChartsViewModelImpl still exists`() {
        assertTrue(
            File("src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsViewModelImpl.kt").exists()
        )
    }

    @Test
    fun `ClubStatsRepository still exists`() {
        assertTrue(
            File("src/main/java/se/kjellstrand/webshooter/data/clubstats/ClubStatsRepository.kt").exists()
        )
    }
}
