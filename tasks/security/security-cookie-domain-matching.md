# MEDIUM — Cookie Domain Matching Too Loose

**Category:** Security
**Priority:** MEDIUM
**Status:** TODO

## Files
- `data/AuthCookieJar.kt:13`

## Issue
Uses `endsWith()` for domain comparison, which could match unrelated domains (e.g., `evil-webshooter.se`).

## Fix
Use strict domain equality or proper cookie domain matching per RFC 6265.
