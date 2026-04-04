# MEDIUM — Unnecessary Recompositions

**Category:** Performance
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/competitions/CompetitionsScreen.kt:94-105` — `navigationItems` list recreated on every recomposition without `remember`.
- `ui/screens/results/ResultsScreen.kt:133-159` — `currentUserIndices` recalculated every recomposition. Should use `derivedStateOf`.

## Fix
Wrap constant or derived values in `remember {}` or `derivedStateOf {}`.
