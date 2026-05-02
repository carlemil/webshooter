package se.kjellstrand.webshooter.di

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DatabaseModuleTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/di/DatabaseModule.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    // --- Fixed behavior: createFromAsset (should FAIL before fix, PASS after fix) ---

    @Test
    fun `database should be created from prebuilt asset`() {
        val source = sourceFile.readText()
        assertTrue(
            "DatabaseModule must use createFromAsset to load the prebuilt database",
            source.contains("createFromAsset")
        )
    }

    @Test
    fun `createFromAsset should reference the correct asset path`() {
        val source = sourceFile.readText()
        assertTrue(
            "createFromAsset must reference databases/webshooter.db",
            source.contains("""createFromAsset("databases/webshooter.db")""")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source file exists`() {
        assertTrue("DatabaseModule.kt should exist", sourceFile.exists())
    }

    @Test
    fun `database module provides AppDatabase`() {
        val source = sourceFile.readText()
        assertTrue(
            "Should provide AppDatabase via Room.databaseBuilder",
            source.contains("Room.databaseBuilder")
        )
    }

    @Test
    fun `database name is webshooter db`() {
        val source = sourceFile.readText()
        assertTrue(
            "Database name should be webshooter.db",
            source.contains("webshooter.db")
        )
    }
}
