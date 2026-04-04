# LOW — Hardcoded Host Header

**Category:** Code Quality
**Priority:** LOW
**Status:** TODO

## Files
- `data/GeneralHeadersInterceptor.kt:18`

## Issue
Host hardcoded to `"webshooter.se"`. If BASE_URL changes, requests break.

## Fix
Extract host from request URL dynamically.
