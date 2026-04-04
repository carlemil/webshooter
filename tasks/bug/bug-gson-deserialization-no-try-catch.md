# LOW — Gson Deserialization Without Try-Catch

**Category:** Bug Risks
**Priority:** LOW
**Status:** TODO

## Files
- `data/results/local/ResultMappers.kt:36`
- `data/competitions/local/CompetitionMappers.kt:91-108`
- `data/mysignups/local/SignupMappers.kt:46-50`
- `data/settings/local/UserProfileMappers.kt:45`

## Issue
`gson.fromJson()` can throw `JsonSyntaxException` if cached JSON is corrupted or schema changes. Not caught.

## Fix
Wrap in try-catch and return null/empty on failure, triggering a fresh network fetch.
