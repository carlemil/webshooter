# MEDIUM — Hardcoded Year

**Category:** UX & Accessibility
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/settings/SettingsScreen.kt:336`

## Issue
`currentYear = 2026` is hardcoded. Will be wrong next year.

## Fix
Use `Calendar.getInstance().get(Calendar.YEAR)`.
