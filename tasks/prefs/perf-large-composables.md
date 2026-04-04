# MEDIUM — Large Composables

**Category:** Performance
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/competitions/CompetitionsScreen.kt:340-535` — `CompetitionItem` is 196 lines with 4 Card components.
- `ui/screens/results/ResultsScreen.kt:278-480` — `ResultsList` is 203 lines mixing grouped and flat view logic.

## Fix
Extract into smaller, focused composable functions.
