# LOW — Logout Dialog Lost on Rotation

**Category:** State Management
**Priority:** LOW
**Status:** TODO

## Files
- `ui/screens/settings/SettingsScreen.kt:131`

## Issue
`showLogoutDialog` is `remember`. If user opens the dialog and rotates, it disappears.

## Fix
Use `rememberSaveable`.
