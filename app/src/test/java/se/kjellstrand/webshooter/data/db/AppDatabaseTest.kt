package se.kjellstrand.webshooter.data.db

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppDatabaseTest {

    private val sourceFile = File("src/main/java/se/kjellstrand/webshooter/data/db/AppDatabase.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `AppDatabase annotation should have exportSchema enabled`() {
        val content = sourceFile.readText()
        assertTrue(
            "exportSchema must be true so Room exports the schema JSON for the prebuilt database task",
            content.contains("exportSchema = true")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `AppDatabase should use DB_VERSION for version`() {
        val content = sourceFile.readText()
        assertTrue(
            "Database version should reference DB_VERSION constant",
            content.contains("version = DB_VERSION")
        )
    }

    @Test
    fun `AppDatabase should declare ResultEntity`() {
        val content = sourceFile.readText()
        assertTrue(
            "Database must include ResultEntity for prebuilt results",
            content.contains("ResultEntity::class")
        )
    }

    @Test
    fun `AppDatabase should extend RoomDatabase`() {
        val content = sourceFile.readText()
        assertTrue(
            "AppDatabase must extend RoomDatabase",
            content.contains("abstract class AppDatabase : RoomDatabase()")
        )
    }
}
