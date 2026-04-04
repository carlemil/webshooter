# CRITICAL — R8/ProGuard Minification Disabled

**Category:** Security
**Priority:** CRITICAL
**Status:** TODO

## Files
- `app/build.gradle.kts:68`

## Issue
`isMinifyEnabled = false` in the release build. All app code is fully readable when decompiled. Combined with the hardcoded secret, this is a significant security gap.

## Fix
Enable `isMinifyEnabled = true` for release builds and add proper ProGuard keep rules.
