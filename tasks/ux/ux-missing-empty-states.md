# HIGH — Missing Empty States

**Category:** UX & Accessibility
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/club/ClubScreen.kt:184-190` — Member list shows spinner indefinitely when the list is empty.
- `ui/screens/results/ResultsScreen.kt:291-297` — When no weapon groups are selected but groups exist, the entire content area is blank.

## Fix
Add explicit empty state messages: "No members found", "No results match your filters", etc.
