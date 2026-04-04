# MEDIUM — Heavy Operations in Composition

**Category:** Performance
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/results/ResultsScreen.kt:369-476` — Large `forEach` loop building grouped results runs during composition, not in a side effect or ViewModel.
- `ui/screens/myresults/MyResultsScreen.kt:220-246` — Complex grid calculation logic runs on every recompose.

## Fix
Move computation to ViewModel or wrap in `remember(dependencies)`.
