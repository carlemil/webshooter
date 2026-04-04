# HIGH — Race Condition on Token Refresh

**Category:** Bug Risks
**Priority:** HIGH
**Status:** TODO

## Files
- `data/AuthTokenManager.kt:101-103`

## Issue
Static mutable token variables accessed without synchronization. Thread A in AuthInterceptor may read a stale token while Thread B updates it in TokenAuthenticator.

## Fix
Use `@Volatile` or `AtomicReference`, or always read from EncryptedSharedPreferences.
