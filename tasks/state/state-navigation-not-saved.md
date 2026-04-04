# MEDIUM — Navigation State Not Saved

**Category:** State Management
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/landingscreen/WebShooterScreen.kt:52-59`

## Issue
`selectedRoute` uses `remember`, not `rememberSaveable`. After rotation, the selected tab may not match the actual navigation state.

## Fix
Use `rememberSaveable` or derive from `navController.currentBackStackEntry`.
