# HIGH — printStackTrace() Instead of Proper Logging

**Category:** Code Quality
**Priority:** HIGH
**Status:** TODO

## Files
All repositories: `LoginRepository`, `CookiesRepository`, `ResultsRepository`, `CompetitionsRepository`, `CompetitionTeamsRepository`, `CompetitionPatrolsRepository`, `CompetitionSignupsRepository`, `SignupsRepository`

## Issue
Exceptions caught with `printStackTrace()` — not captured by Crashlytics, exposes implementation details, not debuggable.

## Fix
Use `Log.e(TAG, message, exception)` or Timber.
