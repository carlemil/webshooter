# MEDIUM — Unsafe Non-Null Assertion

**Category:** Bug Risks
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/signup/SignupScreen.kt:129`

## Issue
`uiState.error!!` — will crash if error is null.

## Fix
Use safe call (`?.`) or `requireNotNull()` with a meaningful message.
