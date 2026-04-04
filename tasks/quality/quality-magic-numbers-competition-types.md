# MEDIUM — Magic Numbers for Competition Types

**Category:** Code Quality
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/competitions/CompetitionsScreen.kt:196-198`
- `ui/screens/patrols/PatrolsScreen.kt:58`
- `ui/screens/myresults/MyResultsScreen.kt:209` — `isFalt = type == "Fält"` hardcoded string comparison.

## Issue
`competitionType.id in setOf(2, 3, 9, 10)` repeated in multiple files with no explanation.

## Fix
Define named constants like `val PATROL_COMPETITION_TYPE_IDS = setOf(2, 3, 9, 10)` in a shared location.
