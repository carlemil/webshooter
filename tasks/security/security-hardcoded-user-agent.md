# MEDIUM — Hardcoded Browser User-Agent

**Category:** Security
**Priority:** MEDIUM
**Status:** TODO

## Files
- `data/GeneralHeadersInterceptor.kt:24-25`

## Issue
Spoofs Chrome/Windows User-Agent. Makes API traffic indistinguishable from browser traffic, may violate ToS, and complicates server-side analytics.

## Fix
Use a proper app-specific User-Agent string like `Webshooter-Android/1.10.0`.
