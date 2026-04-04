# LOW — Unsafe Navigation Argument Extraction

**Category:** Architecture & Navigation
**Priority:** LOW
**Status:** TODO

## Files
- `ui/navigation/AppNavHost.kt:113-115`

## Issue
`remember(backStackEntry)` without null check. `getBackStackEntry()` may return null.

## Fix
Add null safety checks.
