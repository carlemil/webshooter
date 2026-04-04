# CRITICAL — Hardcoded Client Secret

**Category:** Security
**Priority:** CRITICAL
**Status:** TODO

## Files
- `data/login/LoginRepository.kt:32`
- `data/login/remote/RefreshTokenRequest.kt:5`

## Issue
OAuth client_secret `52FphTYzOrmuqH30ltL7LrBzhSEURIJiMFNp6Qt0` is hardcoded in source code, visible in decompiled APK and git history.

## Fix
Move to a secure server-side endpoint or at minimum to BuildConfig with obfuscation.
