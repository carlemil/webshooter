# HIGH — M.-prec tab visibility and label

**Category:** UI
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/charts/resulttrends/ResultsTrendsViewModelImpl.kt:62` (group by tab key, expand availableTypes)
- `ui/screens/charts/resulttrends/ResultsTrendsScreen.kt:125` (custom display name for new key)
- `ui/screens/charts/resulttrends/ResultsTrendsUiState.kt:32` (filteredChartData lookup uses tab key)

## Issue
Today, `loadChartData` groups data points by raw `resultsType` and derives `availableResultsTypes` from `allCompetitionMeta`. This means (a) Magnumprecision data points fall into the Precision bucket and (b) there is no way to add a Magnumprecision tab even when the user has Magnumprecision results. The screen also has no display-name mapping for the new `"magnumprecision"` tab key.

## Fix
1. In `ResultsTrendsViewModelImpl.loadChartData`, change the grouping (line 62):
   ```kotlin
   val grouped = resource.data.dataPoints.groupBy {
       trendsTabKeyFor(it.competitionTypeName, it.resultsType)
   }
   ```
2. Change `availableTypes` derivation (lines 66-71) to:
   - Start with the existing system-wide tabs from metadata (unchanged behavior for precision/military/field).
   - **Append** `MAGNUMPRECISION_TAB_KEY` if and only if any of the user's data points fall into that bucket — i.e. `grouped.containsKey(MAGNUMPRECISION_TAB_KEY)`.
   ```kotlin
   val hiddenTypes = setOf("pointfield")
   val systemTypes = resource.data.allCompetitionMeta.values
       .map { trendsTabKeyFor(it.competitionTypeName, it.resultsType) }
       .distinct()
       .filter { it !in hiddenTypes }
   val availableTypes = (systemTypes + listOfNotNull(
       MAGNUMPRECISION_TAB_KEY.takeIf { grouped.containsKey(it) }
   )).distinct().sorted()
   ```
   This relies on `CompetitionMeta.competitionTypeName` being populated by Task 1.
3. In `ResultsTrendsScreen.kt` (around line 124-127), replace the inline tab label expression with a call to a new helper defined in the same package:
   ```kotlin
   fun trendsTabDisplayName(tabKey: String): String = when (tabKey) {
       MAGNUMPRECISION_TAB_KEY -> "M.-prec"
       else -> ResultsType.fromApiString(tabKey)?.displayName
           ?: tabKey.replaceFirstChar { it.uppercase() }
   }
   ```
   Place it in `TrendsTabKey.kt` (the same file that hosts `trendsTabKeyFor`). Update the `Tab` `text` callback to call `trendsTabDisplayName(type)`.
4. `ChartsUiState.filteredChartData` and `filteredComparedShooters` already key into `chartData` / filter by `selectedResultsType`. Since `chartData` is now keyed by tab key, `filteredChartData` works unchanged. But `filteredComparedShooters` filters compared-shooter points by `it.resultsType == selectedResultsType` — this must change to `trendsTabKeyFor(it.competitionTypeName, it.resultsType) == selectedResultsType` so a compared shooter's Magnumprecision points show in the M.-prec tab.

After the fix, the M.-prec tab appears only when the user has any Magnumprecision data point, the label is "M.-prec", and Magnumprecision points no longer leak into the Precision tab.
