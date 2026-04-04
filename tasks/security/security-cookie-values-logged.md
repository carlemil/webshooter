# LOW — Cookie Values Potentially Logged

**Category:** Security
**Priority:** LOW
**Status:** TODO

## Files
- `data/AuthCookieJar.kt:30-35`

## Issue
`getSessionCookies()` returns raw cookie values. If logged via HttpLoggingInterceptor, session tokens are exposed.

## Fix
Mask cookie values in any debug output.
