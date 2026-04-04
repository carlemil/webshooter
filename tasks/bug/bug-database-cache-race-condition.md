# HIGH — Database Cache Race Condition

**Category:** Bug Risks
**Priority:** HIGH
**Status:** TODO

## Files
- `data/competitions/CompetitionsRepository.kt:85-86`

## Issue
`deleteAll()` then `insertAll()` without a Room `@Transaction`. A concurrent read between the two calls returns empty data.

## Fix
Wrap in a `@Transaction` method on the DAO.
