# Charts Feature — Implementation Summary

**Date:** 2026-04-06
**Commits:** b6fd6a6 → 401375c (7 commits on main)

## Background

The user wanted a new "Diagram" screen in the app's navigation drawer for visualizing shooting competition performance over time. The goal was to show average serie (station) scores per competition as line charts, with the ability to compare results against other club members.

## Requirements Discussion

### What to build
- A new menu item "Diagram" in the navigation drawer
- Line charts showing average serie score per competition over time
- Separate tabs/charts for each competition type (Precision, Falt, Militar, Poangfalt)
- Ability to compare own results with other club members by overlaying their data on the same chart
- Weapon class filtering using clickable WeaponClassBadge components
- MPAndroidChart library (PhilJay/MPAndroidChart) for chart rendering with pinch-zoom and pan

### Clarifying questions and answers

**Q: What is a "serie score"?**
A: The average of points from all StationResult entries for a competition.

**Q: How to group competition types?**
A: Separate tabs/charts for each type (PRECISION, MILITARY, FIELD, POINTS_FIELD).

**Q: How should users select members to compare with?**
A: A search box that matches parts of the user's name, triggered by a "Lagg till skytt" button.

**Q: Data loading strategy — disk only or network?**
A: On first chart load, fetch results from the network for all competitions the user has entered (via MyEntries/Signups), then cache them. Subsequent loads are from disk only. Exception: if competition date is today, always fetch from network.

**Q: Weapon class filtering?**
A: The user should choose what class to see using clickable WeaponClassBadge components that toggle filters.

## Implementation Plan (7 tasks, TDD)

Each task was implemented using a strict red-green TDD cycle: write failing tests first, implement the fix, verify all tests pass, then commit and push.

### Task 1 — Add MPAndroidChart dependency
- Added JitPack repository to `settings.gradle.kts`
- Added `mpandroidchart = "v3.1.0"` to `gradle/libs.versions.toml`
- Added `implementation(libs.mpandroidchart)` to `app/build.gradle.kts`
- **Tests:** 5 (3 fixed-behavior + 2 guard)

### Task 2 — Add Screen route, menu item, and nav wiring
- Added `Screen.Charts` object with route `"charts"` to `Screen.kt`
- Added `"Diagram"` string resource to `strings.xml`
- Added navigation drawer menu item in `WebShooterScreen.kt` (between "Mina resultat" and "Forening")
- Added composable destination in the drawer's NavHost
- **Tests:** 6 (2 fixed-behavior + 4 guard)

### Task 3 — Create chart data aggregation logic
- Created `data/charts/ChartsRepository.kt` with:
  - `ChartDataPoint` data class (competitionId, competitionName, date, averageSerieScore, weaponClass, resultsType)
  - `ChartData` data class wrapping a list of data points
  - `getChartData(userId)` — loads all user signups, fetches/caches results, computes average station scores, groups by resultsType and weaponClass
  - `getShooterChartData(shooterIds, competitionIds)` — loads chart data for multiple shooters for comparison
  - `computeAverageStationScore()` — companion helper for averaging station points
- **Tests:** 9 (5 fixed-behavior + 4 guard)

### Task 4 — Create DI module for ChartsRepository
- Created `di/ChartsModule.kt` with `@Module`, `@InstallIn(SingletonComponent::class)`, and `@Provides @Singleton` for ChartsRepository
- **Tests:** 6 (3 fixed-behavior + 3 guard)

### Task 5 — Create ChartsUiState and ChartsViewModel
- Created `ui/screens/charts/ChartsUiState.kt`:
  - `ShooterChartInfo` data class (name + chartData)
  - `ChartsUiState` with chartData, comparedShooters, selectedResultsType, selectedWeaponClasses, availableWeaponClasses, availableResultsTypes, isLoading, hasError, clubMembers, searchQuery, showSearchDialog
  - Computed properties: `filteredChartData`, `filteredComparedShooters`, `filteredClubMembers`
- Created `ui/screens/charts/ChartsViewModel.kt` interface with selectTab, toggleWeaponClass, addShooter, removeShooter, setSearchQuery, setShowSearchDialog
- Created `ui/screens/charts/ChartsViewModelImpl.kt` as `@HiltViewModel` injecting ChartsRepository, SettingsRepository, ClubRepository
- **Tests:** 10 (6 fixed-behavior + 4 guard)

### Task 6 — Create ChartsScreen composable
- Created `ui/screens/charts/ChartsScreen.kt` with:
  - `ScrollableTabRow` for competition type tabs
  - Clickable `WeaponClassBadge` row for weapon class filtering
  - `ChartLineChart` composable wrapping MPAndroidChart `LineChart` in `AndroidView` with pinch-zoom, drag, multi-shooter line datasets with distinct colors
  - `AddShooterDialog` with search text field, filtered club member list, remove buttons for already-added shooters
  - Loading (CircularProgressIndicator), error, and empty ("Ingen data") states
- Updated `WebShooterScreen.kt` to wire in the real ChartsScreen replacing the placeholder
- Added 7 chart-related string resources (charts_add_shooter, charts_search_shooter, charts_no_data, etc.)
- **Tests:** 7 (3 fixed-behavior + 4 guard)

### Task 7 — Add mock ViewModel and previews
- Created `ui/mock/MockCharts.kt` with sample data for precision, field, and military competition types, mock compared shooters, and mock club members
- Created `ui/mock/ChartsViewModelMock.kt` implementing ChartsViewModel with no-op methods
- Added 5 `@Preview` composables to ChartsScreen: default with data, loading, error, empty, and compared shooters
- **Tests:** 7 (4 fixed-behavior + 3 guard)

## Files Created/Modified

### New files (12)
- `app/src/main/java/se/kjellstrand/webshooter/data/charts/ChartsRepository.kt`
- `app/src/main/java/se/kjellstrand/webshooter/di/ChartsModule.kt`
- `app/src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsUiState.kt`
- `app/src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsViewModel.kt`
- `app/src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsViewModelImpl.kt`
- `app/src/main/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsScreen.kt`
- `app/src/main/java/se/kjellstrand/webshooter/ui/mock/MockCharts.kt`
- `app/src/main/java/se/kjellstrand/webshooter/ui/mock/ChartsViewModelMock.kt`
- `app/src/test/java/se/kjellstrand/webshooter/data/charts/MpAndroidChartDependencyTest.kt`
- `app/src/test/java/se/kjellstrand/webshooter/data/charts/ChartsRepositoryTest.kt`
- `app/src/test/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsViewModelTest.kt`
- `app/src/test/java/se/kjellstrand/webshooter/ui/screens/charts/ChartsScreenTest.kt`

### Modified files (6)
- `settings.gradle.kts` — added JitPack repository
- `gradle/libs.versions.toml` — added mpandroidchart version and library
- `app/build.gradle.kts` — added mpandroidchart dependency
- `app/src/main/java/se/kjellstrand/webshooter/ui/navigation/Screen.kt` — added Charts route
- `app/src/main/java/se/kjellstrand/webshooter/ui/landingscreen/WebShooterScreen.kt` — added menu item and nav destination
- `app/src/main/res/values/strings.xml` — added chart string resources

### Test files (5)
- `MpAndroidChartDependencyTest.kt` — 5 tests
- `ChartsRepositoryTest.kt` — 9 tests
- `ChartsViewModelTest.kt` — 10 tests
- `ChartsScreenTest.kt` — 7 tests
- `ChartsViewModelMockTest.kt` — 7 tests
- Also added: `ScreenChartsRouteTest.kt` — 6 tests, `ChartsModuleTest.kt` — 6 tests

**Total: 50 tests added, all passing.**
