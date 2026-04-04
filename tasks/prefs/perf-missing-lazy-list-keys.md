# HIGH — Missing key() in Lazy Lists

**Category:** Performance
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/competitions/CompetitionsScreen.kt:193`
- `ui/screens/signups/SignupsScreen.kt:124-144`

## Issue
`items(filteredData)` without explicit keys. When list order changes or items are added/removed, Compose may reuse composables incorrectly causing visual glitches or wasted recomposition.

## Fix
Add `key = { it.id }` to all `items()` and `itemsIndexed()` calls.
