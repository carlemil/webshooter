package se.kjellstrand.webshooter

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PrebuiltDatabaseScriptTest {

    private val scriptFile = File("prebuilt-database.gradle.kts")
    private val script: String by lazy { scriptFile.readText() }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `INSERT statement includes userId column`() {
        assertTrue(
            "INSERT INTO results column list must contain userId",
            script.contains("userId")
        )
    }

    @Test
    fun `INSERT statement includes userFullname column`() {
        assertTrue(
            "INSERT INTO results column list must contain userFullname",
            script.contains("userFullname")
        )
    }

    @Test
    fun `INSERT statement includes weaponClassName column`() {
        assertTrue(
            "INSERT INTO results column list must contain weaponClassName",
            script.contains("weaponClassName")
        )
    }

    @Test
    fun `INSERT statement includes averagePoints column`() {
        assertTrue(
            "INSERT INTO results column list must contain averagePoints",
            script.contains("averagePoints")
        )
    }

    @Test
    fun `INSERT statement includes averageHits column`() {
        assertTrue(
            "INSERT INTO results column list must contain averageHits",
            script.contains("averageHits")
        )
    }

    @Test
    fun `INSERT statement uses 16 placeholders for new column count`() {
        // 11 existing + 5 new = 16 placeholders
        val pattern = Regex("""VALUES\s*\(\s*\?(\s*,\s*\?){15}\s*\)""")
        assertTrue(
            "VALUES clause should declare 16 placeholders",
            pattern.containsMatchIn(script)
        )
    }

    @Test
    fun `script extracts user_id from signup user JSON`() {
        assertTrue(
            "script must read user_id from signup.user",
            script.contains("user_id")
        )
    }

    @Test
    fun `script extracts fullname from signup user JSON`() {
        assertTrue(
            "script must read fullname from signup.user",
            script.contains("fullname")
        )
    }

    @Test
    fun `script extracts classname from weaponclass JSON`() {
        assertTrue(
            "script must read classname from weaponclass",
            script.contains("classname")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `INSERT statement still targets the results table`() {
        assertTrue(
            "INSERT INTO results must still be present",
            script.contains("INSERT OR REPLACE INTO results")
        )
    }

    @Test
    fun `INSERT statement still includes the original core columns`() {
        listOf(
            "id", "competitionsId", "signupsId", "placement",
            "figureHits", "hits", "points", "stdMedal",
            "signupJson", "weaponClassJson", "stationResultsJson"
        ).forEach { col ->
            assertTrue(
                "Original column $col must still be in the INSERT list",
                script.contains(col)
            )
        }
    }

    @Test
    fun `script still parses results JSON array per competition`() {
        assertTrue(
            script.contains("getAsJsonArray(\"results\")")
        )
    }
}
