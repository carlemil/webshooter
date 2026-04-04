# CRITICAL — ProGuard Rules Empty

**Category:** Security
**Priority:** CRITICAL
**Status:** TODO

## Files
- `app/proguard-rules.pro`

## Issue
Only default commented-out rules. No keep rules for Room entities, Retrofit models, Hilt, or serialization classes. Enabling minification without fixing this will cause runtime crashes.

## Fix
Add keep rules for data classes, Retrofit interfaces, Room entities, and Hilt components.
