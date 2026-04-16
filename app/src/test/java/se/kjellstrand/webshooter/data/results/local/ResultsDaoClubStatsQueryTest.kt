package se.kjellstrand.webshooter.data.results.local

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ResultsDaoClubStatsQueryTest {

    private val daoFile = File("src/main/java/se/kjellstrand/webshooter/data/results/local/ResultsDao.kt")
    private val clubStatsRowFile = File("src/main/java/se/kjellstrand/webshooter/data/results/local/ClubStatsRow.kt")
    private val daoSource: String by lazy { daoFile.readText() }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ClubStatsRow data class file exists`() {
        assertTrue(
            "ClubStatsRow.kt must exist in data/results/local/",
            clubStatsRowFile.exists()
        )
    }

    @Test
    fun `ClubStatsRow has required fields`() {
        val source = clubStatsRowFile.readText()
        listOf("userId", "fullname", "averagePoints", "competitionCount").forEach { field ->
            assertTrue(
                "ClubStatsRow must declare field $field",
                source.contains(field)
            )
        }
    }

    @Test
    fun `getClubStats query exists in ResultsDao`() {
        assertTrue(
            "ResultsDao must declare getClubStats method",
            daoSource.contains("fun getClubStats(")
        )
    }

    @Test
    fun `getClubStats accepts userIds and year parameters`() {
        val pattern = Regex(
            """fun\s+getClubStats\s*\(\s*userIds\s*:\s*List<Long>\s*,\s*year\s*:\s*Int\s*\)""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getClubStats must accept userIds: List<Long> and year: Int",
            pattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getClubStats query filters to PRECISION resultsType`() {
        // The @Query annotation with SQL appears before `fun getClubStats` in the source
        val queryPattern = Regex(
            """resultsType\s*=\s*'PRECISION'[\s\S]{0,2000}fun\s+getClubStats""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getClubStats query must filter to PRECISION resultsType",
            queryPattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getClubStats query filters to completed status`() {
        val queryPattern = Regex(
            """status\s*=\s*'completed'[\s\S]{0,2000}fun\s+getClubStats""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getClubStats query must filter to completed status",
            queryPattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getClubStats query filters by userId IN userIds`() {
        val queryPattern = Regex(
            """userId\s+IN\s*\(\s*:userIds\s*\)[\s\S]{0,2000}fun\s+getClubStats""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getClubStats query must filter by userId IN (:userIds)",
            queryPattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getClubStats query groups by userId and userFullname`() {
        val queryPattern = Regex(
            """GROUP\s+BY\s+r\.userId\s*,\s*r\.userFullname[\s\S]{0,2000}fun\s+getClubStats""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getClubStats query must GROUP BY r.userId, r.userFullname",
            queryPattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getClubStats query computes AVG averagePoints and COUNT DISTINCT competitionsId`() {
        val avgPattern = Regex(
            """AVG\s*\(\s*r\.averagePoints\s*\)\s+AS\s+averagePoints[\s\S]{0,2000}fun\s+getClubStats""",
            RegexOption.IGNORE_CASE
        )
        val countPattern = Regex(
            """COUNT\s*\(\s*DISTINCT\s+r\.competitionsId\s*\)\s+AS\s+competitionCount[\s\S]{0,2000}fun\s+getClubStats""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getClubStats must compute AVG(r.averagePoints) AS averagePoints",
            avgPattern.containsMatchIn(daoSource)
        )
        assertTrue(
            "getClubStats must compute COUNT(DISTINCT r.competitionsId) AS competitionCount",
            countPattern.containsMatchIn(daoSource)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ResultsDao still has getChartPointsForUser`() {
        assertTrue(daoSource.contains("fun getChartPointsForUser("))
    }

    @Test
    fun `ResultsDao still has getChartPointsForUsers`() {
        assertTrue(daoSource.contains("fun getChartPointsForUsers("))
    }

    @Test
    fun `ResultsDao still has getAllParticipants`() {
        assertTrue(daoSource.contains("fun getAllParticipants("))
    }

    @Test
    fun `ResultsDao still has getPrecisionParticipants`() {
        assertTrue(daoSource.contains("fun getPrecisionParticipants("))
    }

    @Test
    fun `ResultsDao is annotated with Dao`() {
        assertTrue(daoSource.contains("@Dao"))
    }
}
