package se.kjellstrand.webshooter.data.clubstats

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClubStatsWeaponFilterTest {

    private val daoFile = File("src/main/java/se/kjellstrand/webshooter/data/results/local/ResultsDao.kt")
    private val repoFile = File("src/main/java/se/kjellstrand/webshooter/data/clubstats/ClubStatsRepository.kt")
    private val uiStateFile = File("src/main/java/se/kjellstrand/webshooter/ui/screens/clubstats/ClubStatsUiState.kt")
    private val vmFile = File("src/main/java/se/kjellstrand/webshooter/ui/screens/clubstats/ClubStatsViewModel.kt")
    private val vmImplFile = File("src/main/java/se/kjellstrand/webshooter/ui/screens/clubstats/ClubStatsViewModelImpl.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ResultsDao getClubStats accepts classPrefix and filters by weaponClassName LIKE`() {
        val source = daoFile.readText()
        assertTrue(
            "getClubStats signature must accept classPrefix: String",
            Regex(
                """suspend fun getClubStats\([^)]*classPrefix\s*:\s*String[^)]*\)"""
            ).containsMatchIn(source)
        )
        assertTrue(
            "getClubStats query must filter by r.weaponClassName LIKE :classPrefix",
            source.contains("r.weaponClassName LIKE :classPrefix")
        )
    }

    @Test
    fun `ClubStatsRepository getClubStats accepts optional WeaponClassGroup and maps to prefix`() {
        val source = repoFile.readText()
        assertTrue(
            "Repository must import or reference WeaponClassGroup",
            source.contains("WeaponClassGroup")
        )
        assertTrue(
            "Repository getClubStats must accept a nullable WeaponClassGroup parameter",
            Regex(
                """fun getClubStats\([^)]*WeaponClassGroup\?[^)]*\)"""
            ).containsMatchIn(source)
        )
        assertTrue(
            "Repository must pass a classPrefix argument (e.g., '%' for all or 'A%'/'B%'/'C%') through to the DAO",
            source.contains("classPrefix") || source.contains("prefix")
        )
    }

    @Test
    fun `ClubStatsUiState exposes selectedGroup and availableGroups`() {
        val source = uiStateFile.readText()
        assertTrue(
            "ClubStatsUiState must expose selectedGroup: WeaponClassGroup?",
            Regex("""selectedGroup\s*:\s*WeaponClassGroup\?""").containsMatchIn(source)
        )
        assertTrue(
            "ClubStatsUiState must expose availableGroups: Set<WeaponClassGroup>",
            Regex("""availableGroups\s*:\s*Set<WeaponClassGroup>""").containsMatchIn(source)
        )
    }

    @Test
    fun `ClubStatsViewModel interface declares selectWeaponGroup`() {
        val source = vmFile.readText()
        assertTrue(
            "ClubStatsViewModel interface must declare fun selectWeaponGroup(group: WeaponClassGroup?)",
            Regex(
                """fun selectWeaponGroup\([^)]*WeaponClassGroup\?[^)]*\)"""
            ).containsMatchIn(source)
        )
    }

    @Test
    fun `ClubStatsViewModelImpl overrides selectWeaponGroup and reloads with selected group`() {
        val source = vmImplFile.readText()
        assertTrue(
            "ClubStatsViewModelImpl must override selectWeaponGroup",
            source.contains("override fun selectWeaponGroup")
        )
        assertTrue(
            "ClubStatsViewModelImpl must pass the group to the repository on reload",
            source.contains("clubStatsRepository.getClubStats(") &&
                source.contains("selectedGroup")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ClubStatsRepository still depends on ResultsDao and ClubRepository`() {
        val source = repoFile.readText()
        assertTrue(source.contains("ResultsDao"))
        assertTrue(source.contains("ClubRepository"))
    }

    @Test
    fun `ClubStatsUiState still holds shooterStats and year`() {
        val source = uiStateFile.readText()
        assertTrue(source.contains("shooterStats: List<ShooterStats>"))
        assertTrue(source.contains("year: Int"))
    }

    @Test
    fun `ClubStatsViewModelImpl is still annotated HiltViewModel`() {
        val source = vmImplFile.readText()
        assertTrue(source.contains("@HiltViewModel"))
    }
}
