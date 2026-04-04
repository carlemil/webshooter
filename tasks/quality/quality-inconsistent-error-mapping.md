# LOW — Inconsistent Error Mapping

**Category:** Code Quality
**Priority:** LOW
**Status:** TODO

## Issue
All repositories map to generic `UserError.IOError`, `UserError.HttpError`, `UserError.UnknownError` — no distinction between 400, 401, 403, 404, 500.

## Fix
Expand `UserError` to include HTTP status codes for specific error recovery.
