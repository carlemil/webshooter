# MEDIUM — Navigation State vs NavController Mismatch

**Category:** Architecture & Navigation
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/landingscreen/WebShooterScreen.kt:107-122`

## Issue
Manual route tracking via `selectedRoute` string. Hardware back button can desync this from NavController's actual back stack.

## Fix
Derive selected state from `navController.currentBackStackEntryAsState()`.
