package se.kjellstrand.webshooter.data.results.local

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ResultEntityTest {

    private val sourceFile = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/data/results/local/ResultEntity.kt")
    private val source: String by lazy { sourceFile.readText() }

    private fun fieldType(name: String): String? =
        ResultEntity::class.java.declaredFields
            .firstOrNull { it.name == name }
            ?.type
            ?.name

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ResultEntity has userId Long column`() {
        assertEquals("long", fieldType("userId"))
    }

    @Test
    fun `ResultEntity has userFullname String column`() {
        assertEquals("java.lang.String", fieldType("userFullname"))
    }

    @Test
    fun `ResultEntity has weaponClassName String column`() {
        assertEquals("java.lang.String", fieldType("weaponClassName"))
    }

    @Test
    fun `ResultEntity has averagePoints Double column`() {
        assertEquals("double", fieldType("averagePoints"))
    }

    @Test
    fun `ResultEntity has averageHits Double column`() {
        assertEquals("double", fieldType("averageHits"))
    }

    @Test
    fun `ResultEntity declares composite index on userId and competitionsId`() {
        val pattern = Regex(
            """Index\s*\(\s*value\s*=\s*\[\s*"userId"\s*,\s*"competitionsId"\s*]"""
        )
        assertTrue(
            "Expected an Index annotation on (userId, competitionsId)",
            pattern.containsMatchIn(source)
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ResultEntity table name is results`() {
        assertTrue(
            "Expected tableName = \"results\" in @Entity",
            source.contains("tableName = \"results\"")
        )
    }

    @Test
    fun `ResultEntity still exposes existing JSON columns`() {
        assertEquals("java.lang.String", fieldType("signupJson"))
        assertEquals("java.lang.String", fieldType("weaponClassJson"))
        assertEquals("java.lang.String", fieldType("stationResultsJson"))
    }

    @Test
    fun `ResultEntity still exposes core scalar columns`() {
        assertEquals("long", fieldType("id"))
        assertEquals("long", fieldType("competitionsId"))
        assertEquals("long", fieldType("signupsId"))
        assertEquals("long", fieldType("points"))
    }
}
