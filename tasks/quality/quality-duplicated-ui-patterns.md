# MEDIUM — Duplicated UI Patterns

**Category:** Code Quality
**Priority:** MEDIUM
**Status:** TODO

## Files
- **Weapon class badge cards:** `PatrolsScreen.kt:142-214` and `SignupsScreen.kt:172-200`
- **Error/empty state handling:** Inconsistent across `MyResultsScreen`, `ClubScreen`, `PatrolsScreen`
- **Filter bottom sheet:** Similar structure in `ResultsScreen.kt:534-606` and `SignupsScreen.kt:203-298`

## Fix
Extract shared composables: `EmptyStateBox`, `ErrorStateCard`, `FilterBottomSheet`.
