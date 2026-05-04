package se.kjellstrand.webshooter.data.db

/**
 * Bump together with the Android app's versionCode. The prebuilt asset
 * database under app/src/main/assets/databases/webshooter.db is regenerated
 * each release, and Room destroys-and-recreates on a version mismatch.
 */
const val DB_VERSION: Int = 32
