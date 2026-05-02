# HIGH — Expand WeaponClassGroup enum (R + M1-M9)

**Category:** UI
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/charts/seriespoints/SeriesPointsUiState.kt:7` (modify enum)
- `ui/common/WeaponClassGroupFilter.kt:29` (modify rendering)

## Issue
`WeaponClassGroup` only has `A`, `B`, `C` with a single-Char prefix. The fält tab needs an `R` group (matching all R1/R2/R3 weapon classes), and the new M.-prec tab needs nine separate groups `M1`..`M9` matching weapon classes by 2-char prefix. The current enum signature can't express 2-char prefixes, and the filter component renders **all** enum values (only disabling those not in `availableGroups`), which would produce 13 disabled ghost buttons on every tab.

## Fix
1. In `SeriesPointsUiState.kt`:
   - Change the enum constructor parameter from `prefix: Char` to `prefix: String`.
   - Change `matches` to `weaponClass.startsWith(prefix, ignoreCase = true)`.
   - Add entries: `R("R")`, `M1("M1")`, `M2("M2")`, `M3("M3")`, `M4("M4")`, `M5("M5")`, `M6("M6")`, `M7("M7")`, `M8("M8")`, `M9("M9")`. Final order: `A, B, C, R, M1, M2, M3, M4, M5, M6, M7, M8, M9`.
2. In `WeaponClassGroupFilter.kt`:
   - Change `WeaponClassGroup.values().forEach { group -> ... }` to iterate over `availableGroups.sortedBy { it.ordinal }.forEach { group -> ... }` (note: `Set<WeaponClassGroup>` has no inherent ordering, so sort by ordinal to keep a stable A→B→C→R→M1…M9 layout).
   - Drop the `enabled = group in availableGroups` flag and the disabled-state styling — only enabled groups are now rendered. The radio still uses `selected = group == selectedGroup` and `onClick = { onSelectGroup(group) }`.
   - Keep the label as `"${group.prefix}*"` — this still renders correctly ("A*", "M1*", "R*").
3. Do **not** move `WeaponClassGroup` out of the `seriespoints` package — leave the existing import paths intact across the codebase.

Side effect to confirm: SeriesPoints currently passes a dynamically-computed `availableGroups` set to the filter. With the rendering change, SeriesPoints will now hide groups its user has no shots in (instead of showing them disabled). That matches reasonable UX and the user explicitly chose this behavior in planning.
