package se.kjellstrand.webshooter.di

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DatabaseModuleTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/di/DatabaseModule.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `destructive migration is conditional on debug build`() {
        val source = sourceFile.readText()
        val hasUnconditionalDestructive = Regex(
            """\.\s*fallbackToDestructiveMigration\(\)"""
        ).containsMatchIn(source)
        val hasConditionalDestructive = source.contains("BuildConfig.DEBUG") &&
                source.contains("fallbackToDestructiveMigration")
        assertTrue(
            "fallbackToDestructiveMigration should be conditional on BuildConfig.DEBUG",
            hasConditionalDestructive && !hasUnconditionalDestructive
        )
    }

    @Test
    fun `database module references BuildConfig for migration strategy`() {
        val source = sourceFile.readText()
        assertTrue(
            "DatabaseModule should import or reference BuildConfig",
            source.contains("BuildConfig")
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
