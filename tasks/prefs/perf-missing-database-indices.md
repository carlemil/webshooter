# LOW — Missing Database Indices

**Category:** Performance
**Priority:** LOW
**Status:** TODO

## Files
- `data/competitionteams/local/TeamEntity.kt`
- `data/competitionpatrols/local/PatrolEntity.kt`
- `data/competitionsignups/local/CompetitionSignupEntity.kt`

## Issue
Queries filter by `competitionId` frequently but no `@Index` is defined, causing full table scans.

## Fix
Add `indices = [Index("competitionId")]` to `@Entity` annotations.
