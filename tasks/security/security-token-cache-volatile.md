# MEDIUM — In-Memory Token Cache Without Volatile

**Category:** Security
**Priority:** MEDIUM
**Status:** TODO

## Files
- `data/AuthTokenManager.kt:101-103`

## Issue
Companion object vars `token`, `refreshToken`, `tokenExpiresAtMillis` are read/written from multiple threads without `@Volatile` or synchronization.

## Fix
Add `@Volatile` to all three fields, or synchronize access.
