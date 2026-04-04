# HIGH — HTTP Logging in Production

**Category:** Security
**Priority:** HIGH
**Status:** TODO

## Files
- `di/NetworkModule.kt:52`

## Issue
`HttpLoggingInterceptor` at `Level.BASIC` runs in release builds. May log tokens and sensitive headers.

## Fix
Conditionally set level to `NONE` when `!BuildConfig.DEBUG`.
