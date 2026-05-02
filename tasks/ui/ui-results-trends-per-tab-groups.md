# HIGH — Per-tab availableGroups and default selectedGroup

**Category:** UI
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/charts/resulttrends/ResultsTrendsUiState.kt:14` (modify defaults; add helpers)
- `ui/screens/charts/resulttrends/ResultsTrendsViewModelImpl.kt:56` (apply on initial load)
- `ui/screens/charts/resulttrends/ResultsTrendsViewModelImpl.kt:115` (apply on selectTab)

## Issue
`ChartsUiState.availableGroups` is hardcoded to `{A, B, C}` and `selectedGroup` defaults to `C` regardless of which tab is active. The Fält tab needs to show `{A, B, C, R}`, the new M.-prec tab needs `{M1..M9}` with default `M7`, and other tabs keep the current `{A, B, C}` with default `C`. The ViewModel must update both fields whenever the active tab changes.

## Fix
1. In `ResultsTrendsUiState.kt`, add two top-level helpers in the same file (outside the data class):

   ```kotlin
   fun groupsForTab(tabKey: String): Set<WeaponClassGroup> = when (tabKey) {
       "field" -> setOf(WeaponClassGroup.A, WeaponClassGroup.B, WeaponClassGroup.C, WeaponClassGroup.R)
       MAGNUMPRECISION_TAB_KEY -> setOf(
           WeaponClassGroup.M1, WeaponClassGroup.M2, WeaponClassGroup.M3,
           WeaponClassGroup.M4, WeaponClassGroup.M5, WeaponClassGroup.M6,
           WeaponClassGroup.M7, WeaponClassGroup.M8, WeaponClassGroup.M9,
       )
       else -> setOf(WeaponClassGroup.A, WeaponClassGroup.B, WeaponClassGroup.C)
   }

   fun defaultGroupForTab(tabKey: String): WeaponClassGroup = when (tabKey) {
       MAGNUMPRECISION_TAB_KEY -> WeaponClassGroup.M7
       else -> WeaponClassGroup.C
   }
   ```

   Import `MAGNUMPRECISION_TAB_KEY` from `TrendsTabKey.kt`.

2. In `ResultsTrendsViewModelImpl.loadChartData` (around line 72-82), after `selectedType` is computed:
   - Compute `val effectiveGroups = groupsForTab(selectedType)`.
   - Compute the new selected group: keep the current one if it's still valid for the new tab, otherwise reset to `defaultGroupForTab(selectedType)`. Concretely:
     ```kotlin
     val currentGroup = _uiState.value.selectedGroup
     val newGroup = if (currentGroup != null && currentGroup in effectiveGroups) currentGroup
                    else defaultGroupForTab(selectedType)
     ```
   - Pass both `availableGroups = effectiveGroups` and `selectedGroup = newGroup` into the `_uiState.value.copy(...)` call.

3. In `selectTab` (line 115), replace the body:
   ```kotlin
   override fun selectTab(resultsType: String) {
       val groups = groupsForTab(resultsType)
       _uiState.value = _uiState.value.copy(
           selectedResultsType = resultsType,
           availableGroups = groups,
           selectedGroup = defaultGroupForTab(resultsType)
       )
   }
   ```
   Always reset to the default group on tab switch — simpler and matches the user's expectation of "default to M7 on M.-prec".

4. The default value of `availableGroups` in `ChartsUiState` (currently `{A, B, C}`) can stay as a sane fallback for the initial loading state.
