package se.kjellstrand.webshooter.di

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DatabaseModuleTest {

    private val moduleFile = File("src/main/java/se/kjellstrand/webshooter/di/DatabaseModule.kt")
    private val factoryFile = File("../shared/src/androidMain/kotlin/se/kjellstrand/webshooter/data/db/DatabaseFactory.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `database should be created from prebuilt asset`() {
        val source = factoryFile.readText()
        assertTrue(
            "Database factory must use createFromAsset to load the prebuilt database",
            source.contains("createFromAsset")
        )
    }

    @Test
    fun `createFromAsset should reference the correct asset path`() {
        val source = factoryFile.readText()
        assertTrue(
            "createFromAsset must reference databases/webshooter.db",
            source.contains("""createFromAsset("databases/webshooter.db")""")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source files exist`() {
        assertTrue("DatabaseModule.kt should exist", moduleFile.exists())
        assertTrue("DatabaseFactory.kt should exist", factoryFile.exists())
    }

    @Test
    fun `database module forwards to the shared factory`() {
        val source = moduleFile.readText()
        assertTrue(
            "DatabaseModule should call createAppDatabase(context) from :shared",
            source.contains("createAppDatabase(context)")
        )
    }

    @Test
    fun `database factory builds a Room database`() {
        val source = factoryFile.readText()
        assertTrue(
            "Factory should provide AppDatabase via Room.databaseBuilder",
            source.contains("Room.databaseBuilder")
        )
    }

    @Test
    fun `database name is webshooter db`() {
        val source = factoryFile.readText()
        assertTrue(
            "Database name should be webshooter.db",
            source.contains("webshooter.db")
        )
    }
}
