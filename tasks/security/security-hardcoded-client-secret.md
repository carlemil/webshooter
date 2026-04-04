# CRITICAL — Hardcoded Client Secret

**Category:** Security
**Priority:** CRITICAL
**Status:** TODO

## Files
- `data/login/LoginRepository.kt:32`
- `data/login/remote/RefreshTokenRequest.kt:5`

## Issue
OAuth client_secret `REMOVED-CLIENT-SECRET` is hardcoded in source code, visible in decompiled APK and git history.

## Fix
Move to a secure server-side endpoint or at minimum to BuildConfig with obfuscation.
