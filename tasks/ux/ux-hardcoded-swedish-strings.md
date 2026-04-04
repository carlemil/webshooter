# MEDIUM — Hardcoded Swedish Strings

**Category:** UX & Accessibility
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/results/ResultsScreen.kt:548, 562-567`

## Issue
"Gruppering", "Vapenklass", "Klubb", "Medl", "Ingen" are hardcoded strings not going through `stringResource()`.

## Fix
Move all user-facing text to `strings.xml`.
