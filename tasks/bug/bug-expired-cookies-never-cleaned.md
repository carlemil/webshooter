# MEDIUM — Expired Cookies Never Cleaned

**Category:** Bug Risks
**Priority:** MEDIUM
**Status:** TODO

## Files
- `data/AuthCookieJar.kt`

## Issue
Cookies are stored indefinitely in a HashMap. `loadForRequest()` never checks `cookie.expiresAt`. Stale cookies accumulate.

## Fix
Filter out expired cookies in `loadForRequest()`.
