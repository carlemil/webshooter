# HIGH — Inconsistent competitionId Types (Int vs Long)

**Category:** Bug Risks
**Priority:** HIGH
**Status:** TODO

## Files
- `data/results/ResultsRepository.kt:24` (Int)
- `data/competitionteams/CompetitionTeamsRepository.kt:24` (Long)
- `data/competitionpatrols/CompetitionPatrolsRepository.kt:24` (Long)
- `ui/navigation/AppNavHost.kt:66` (IntType) vs `:87` (LongType)

## Issue
Results module uses `Int` for competitionId while others use `Long`. Navigation routes also mix types. IDs exceeding `Int.MAX_VALUE` will silently overflow.

## Fix
Standardize all competitionId parameters to `Long` across the entire codebase.
