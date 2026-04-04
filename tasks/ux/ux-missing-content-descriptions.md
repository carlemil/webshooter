# HIGH — Missing Content Descriptions

**Category:** UX & Accessibility
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/login/LoginScreen.kt:118` — Password visibility toggle icon has `contentDescription = null`.
- `ui/screens/settings/SettingsScreen.kt:447` — Password visibility toggle icon has `contentDescription = null`.
- `ui/screens/signup/SignupScreen.kt:60` — Back button has hardcoded `"Back"` instead of `stringResource`.

## Fix
Add proper `stringResource(R.string.xxx)` content descriptions to all interactive icons.
