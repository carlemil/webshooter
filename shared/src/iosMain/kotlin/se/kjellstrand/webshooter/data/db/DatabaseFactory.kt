package se.kjellstrand.webshooter.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS factory: builds the Webshooter Room database in the app's
 * Documents directory using the bundled SQLite driver. Schema mismatches
 * drop and recreate the database; rows are rehydrated from the network
 * on the next sync.
 *
 * Mirrors the Android safety net for cases where the on-disk DB file is
 * unreadable (file-system corruption, partial write, manual tampering on
 * Simulator): wrap `.build()` in try/catch, delete the file, rebuild.
 * Schema mismatches that survive `fallbackToDestructiveMigration` would
 * surface here on the next DAO call rather than in [build]; this guards
 * the file-system path only.
 */
@OptIn(ExperimentalForeignApi::class)
fun createAppDatabase(): AppDatabase {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.path ?: error("Unable to resolve iOS Documents directory for Room database")
    val dbPath = "$documents/webshooter.db"

    return try {
        build(dbPath)
    } catch (e: RuntimeException) {
        Napier.w("Room DB at $dbPath unusable, deleting and rebuilding", e, tag = TAG)
        NSFileManager.defaultManager.removeItemAtPath(dbPath, null)
        build(dbPath)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun build(dbPath: String): AppDatabase =
    Room.databaseBuilder<AppDatabase>(name = dbPath)
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

private const val TAG = "DatabaseFactory"
