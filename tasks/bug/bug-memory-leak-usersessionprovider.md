# MEDIUM — Memory Leak in UserSessionProvider

**Category:** Bug Risks
**Priority:** MEDIUM
**Status:** TODO

## Files
- `data/UserSessionProvider.kt:20-30`

## Issue
`CoroutineScope(SupervisorJob() + Dispatchers.IO)` is created but never cancelled. Jobs accumulate over the app lifetime.

## Fix
Implement `Closeable` and cancel the scope, or tie to a lifecycle-aware scope.
