# HIGH — Modify DatabaseModule to use createFromAsset

**Category:** DI
**Priority:** HIGH
**Status:** TODO

## Files
- `di/DatabaseModule.kt:28-31`

## Issue
The Room database is built empty on first install. Users see no historical results until data is fetched from the API.

## Fix
In `DatabaseModule.provideDatabase()`, add `.createFromAsset("databases/webshooter.db")` to the builder chain:

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "webshooter.db")
    .createFromAsset("databases/webshooter.db")
    .fallbackToDestructiveMigration()
    .build()
```

This copies the prebuilt database from assets on first install. On subsequent launches the existing database is used. `fallbackToDestructiveMigration()` handles schema changes across version upgrades.
