package se.kjellstrand.webshooter.data.results.local

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ResultsDaoChartQueriesTest {

    private val daoFile = File("src/main/java/se/kjellstrand/webshooter/data/results/local/ResultsDao.kt")
    private val rowFile = File("src/main/java/se/kjellstrand/webshooter/data/results/local/ChartPointRow.kt")
    private val daoSource: String by lazy { daoFile.readText() }
    private val rowSource: String by lazy { rowFile.readText() }

    private fun fieldNames(clazz: Class<*>): List<String> =
        clazz.declaredFields.map { it.name }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ChartPointRow data class exposes the projection columns`() {
        val fields = fieldNames(ChartPointRow::class.java)
        listOf(
            "competitionId",
            "competitionName",
            "date",
            "resultsType",
            "weaponClassName",
            "userId",
            "averageScore"
        ).forEach { name ->
            assertTrue("ChartPointRow must declare field $name", fields.contains(name))
        }
    }

    @Test
    fun `ParticipantRow data class exposes userId and fullname`() {
        val fields = fieldNames(ParticipantRow::class.java)
        assertTrue(fields.contains("userId"))
        assertTrue(fields.contains("fullname"))
    }

    @Test
    fun `getChartPointsForUser query joins results with competitions on id`() {
        val pattern = Regex(
            """getChartPointsForUser[\s\S]{0,1500}FROM\s+results\s+r\s+INNER\s+JOIN\s+competitions\s+c\s+ON\s+c\.id\s*=\s*r\.competitionsId""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getChartPointsForUser must JOIN results to competitions on id",
            pattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getChartPointsForUser query restricts to completed competitions and the user`() {
        val pattern = Regex(
            """WHERE\s+r\.userId\s*=\s*:userId\s+AND\s+c\.status\s*=\s*'completed'""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getChartPointsForUser must filter by user id and completed status",
            pattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getChartPointsForUser query selects averageHits for field types and averagePoints otherwise`() {
        val pattern = Regex(
            """CASE\s+WHEN\s+c\.resultsType\s+IN\s*\(\s*'field'\s*,\s*'pointfield'\s*\)\s+THEN\s+r\.averageHits\s+ELSE\s+r\.averagePoints\s+END\s+AS\s+averageScore""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "averageScore projection must use CASE on resultsType",
            pattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getChartPointsForUsers query filters by user id set and competition id set`() {
        val pattern = Regex(
            """WHERE\s+r\.userId\s+IN\s*\(\s*:userIds\s*\)\s+AND\s+r\.competitionsId\s+IN\s*\(\s*:competitionIds\s*\)""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getChartPointsForUsers must filter by both id collections",
            pattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getAllParticipants query selects distinct user id and fullname ordered by name`() {
        val pattern = Regex(
            """SELECT\s+DISTINCT\s+userId\s*,\s*userFullname\s+AS\s+fullname\s+FROM\s+results\s+ORDER\s+BY\s+userFullname""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getAllParticipants must SELECT DISTINCT userId, userFullname AS fullname",
            pattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `getAllWeaponClasses query selects distinct weapon class names ordered alphabetically`() {
        val pattern = Regex(
            """SELECT\s+DISTINCT\s+weaponClassName\s+FROM\s+results\s+ORDER\s+BY\s+weaponClassName""",
            RegexOption.IGNORE_CASE
        )
        assertTrue(
            "getAllWeaponClasses must SELECT DISTINCT weaponClassName ordered",
            pattern.containsMatchIn(daoSource)
        )
    }

    @Test
    fun `dao does not contain stub WHERE 1 = 0 placeholders`() {
        assertFalse(
            "Stub queries (WHERE 1 = 0) must be replaced by real implementations",
            daoSource.contains("1 = 0")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ResultsDao still exposes getByCompetition`() {
        assertTrue(daoSource.contains("fun getByCompetition(competitionId: Long)"))
    }

    @Test
    fun `ResultsDao still exposes insertAll and deleteByCompetition`() {
        assertTrue(daoSource.contains("fun insertAll(results: List<ResultEntity>)"))
        assertTrue(daoSource.contains("fun deleteByCompetition(competitionId: Long)"))
    }

    @Test
    fun `ChartPointRow source is in the results local package`() {
        assertTrue(rowSource.contains("package se.kjellstrand.webshooter.data.results.local"))
    }
}
