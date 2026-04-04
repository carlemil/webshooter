# HIGH — State Lost on Configuration Change

**Category:** State Management
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/signups/SignupsScreen.kt:108-123` — Local state (dropdown expanded, note text, selected weapon class) uses `remember` instead of `rememberSaveable`. Rotation destroys all user input.
- `ui/screens/results/ResultsScreen.kt:92-106` — `isFilterBottomSheetOpen`, `secondsLeft`, `refreshTrigger`, `occurrenceIdx` not saved across config changes.

## Fix
Use `rememberSaveable` for all user-interactive state, or move state to ViewModel with `SavedStateHandle`.
