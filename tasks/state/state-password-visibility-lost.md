# MEDIUM — Password Visibility Lost on Rotation

**Category:** State Management
**Priority:** MEDIUM
**Status:** TODO

## Files
- `ui/screens/login/LoginScreen.kt:52-54`

## Issue
`passwordVisible` uses `remember` — rotation hides the password again mid-typing.

## Fix
Use `rememberSaveable`.
