# CRITICAL — Near-Zero Test Coverage

**Category:** Testing
**Priority:** CRITICAL
**Status:** TODO

## Files
- `app/src/androidTest/.../StationResultRemoteDataSourceTest.kt` (76 lines, 1 test)

## Issue
The entire app has one test file with a single test method. No unit tests exist (`src/test/` is empty).

## Recommended Test Additions
- **Unit tests:** All ViewModels (state transitions, error handling), all Repositories (cache logic, error mapping), mappers (entity <-> domain)
- **Integration tests:** Auth flow (login -> token storage -> refresh), navigation (screen transitions, argument passing)
- **UI tests:** Login screen, competition list, results display
