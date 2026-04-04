# MEDIUM — Fallback Navigation Value -1L

**Category:** Bug Risks
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/navigation/AppNavHost.kt:116`

## Issue
`arguments?.getLong("competitionId") ?: -1L` — if argument is missing, -1L is silently passed downstream causing confusing API errors.

## Fix
Navigate back on missing required arguments and show a error message to the user.
