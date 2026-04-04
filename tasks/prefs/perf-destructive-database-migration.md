# LOW — Destructive Database Migration

**Category:** Performance
**Priority:** LOW
**Status:** TODO

## Files
- `di/DatabaseModule.kt:29`

## Issue
`.fallbackToDestructiveMigration()` deletes all cached data on any schema change.

## Fix
Implement proper Room migrations for production; only use destructive for debug builds.
