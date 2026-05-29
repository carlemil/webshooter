package se.kjellstrand.webshooter.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS factory: builds the Webshooter Room database in the app's
 * Documents directory using the bundled SQLite driver. Schema mismatches
 * drop and recreate the database; rows are rehydrated from the network
 * on the next sync.
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

    return Room.databaseBuilder<AppDatabase>(name = dbPath)
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
