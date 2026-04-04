# LOW — String "null" Handling

**Category:** Bug Risks
**Priority:** LOW
**Status:** TODO

## Files
- `ui/screens/club/ClubScreen.kt:102-110`

## Issue
`.takeIf { it != "null" }` to handle the literal string "null" from the API. Fragile workaround.

## Fix
Fix the data model to use nullable types properly, or handle at the repository/mapper layer.
