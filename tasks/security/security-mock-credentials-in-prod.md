# HIGH — Mock Credentials in Production Build

**Category:** Security
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/login/LoginViewModel.kt:41-50`

## Issue
Hardcoded `mockuser`/`mockpassword` check exists in production. Anyone knowing these credentials can bypass login and enter mock mode.

## Fix
Guard behind `BuildConfig.DEBUG` or remove from release builds entirely.
