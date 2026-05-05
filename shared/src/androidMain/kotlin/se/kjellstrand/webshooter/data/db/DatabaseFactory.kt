package se.kjellstrand.webshooter.data.db

import android.content.Context
import androidx.room.Room

/**
 * Android factory: builds the Webshooter Room database, seeded from the
 * bundled `assets/databases/webshooter.db` snapshot generated at build time
 * by `prebuilt-database.gradle.kts`. Schema mismatches drop and recreate
 * the database (the seed file ships with the app, so the next sync will
 * rehydrate any user-specific rows from the network).
 */
fun createAppDatabase(context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, "webshooter.db")
        .createFromAsset("databases/webshooter.db")
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
