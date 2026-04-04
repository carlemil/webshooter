# MEDIUM — Repository Cache Pattern Duplication

**Category:** Code Quality
**Priority:** MEDIUM
**Status:** TODO

## Files
All competition-related repositories duplicate ~50 lines of cache-remote-sync logic.

## Issue
`CachedResourceFlow.kt` utility exists but is unused.

## Fix
Refactor repositories to use the existing `cachedResourceFlow()` utility.
