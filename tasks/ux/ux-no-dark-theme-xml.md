# LOW — No Dark Theme for XML Resources

**Category:** UX & Accessibility
**Priority:** LOW
**Status:** TODO

## Files
- `res/values/themes.xml:4`

## Issue
XML theme only defines light. Compose theme handles dark mode, but splash screen and system bars may not follow.

## Fix
Add `values-night/themes.xml` for system-level dark theme consistency.
