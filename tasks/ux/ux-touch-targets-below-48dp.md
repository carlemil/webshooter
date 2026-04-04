# MEDIUM — Touch Targets Below 48dp Minimum

**Category:** UX & Accessibility
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/myresults/MyResultsScreen.kt:190`

## Issue
IconButton with `size(20.dp)` — well below the 48dp accessibility minimum for touch targets.

## Fix
Use `Modifier.size(48.dp)` for the touch target; visually size the icon smaller inside.
