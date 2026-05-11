package se.kjellstrand.webshooter.data.db

import android.content.Context
import android.util.Log
import androidx.room.Room

private const val DB_NAME = "webshooter.db"
private const val ASSET_PATH = "databases/webshooter.db"
private const val TAG = "DatabaseFactory"

/**
 * Android factory: builds the Webshooter Room database, normally seeded from
 * the bundled `assets/databases/webshooter.db` snapshot generated at build
 * time by `prebuilt-database.gradle.kts`.
 *
 * If the bundled asset's schema doesn't match the current `@Database`
 * (typical when [DB_VERSION] is bumped but the asset hasn't been
 * regenerated against the new schema), Room throws `IllegalStateException`
 * during `.createFromAsset(...)` validation **before** any migration logic
 * runs. We catch it, drop the seeded file, and rebuild from scratch — the
 * next network sync rehydrates everything.
 */
fun createAppDatabase(context: Context): AppDatabase = try {
    buildWithAsset(context).also { it.openHelper.writableDatabase }
} catch (e: IllegalStateException) {
    Log.w(TAG, "Bundled DB asset schema mismatch — rebuilding without asset", e)
    context.deleteDatabase(DB_NAME)
    buildWithoutAsset(context)
}

private fun buildWithAsset(context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
        .createFromAsset(ASSET_PATH)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

private fun buildWithoutAsset(context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
