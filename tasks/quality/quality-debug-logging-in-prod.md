# HIGH — Debug Logging Left in Production

**Category:** Code Quality
**Priority:** HIGH
**Status:** TODO

## Files
- `ui/screens/shooterresult/ShooterResultScreen.kt:50` — `Log.d()`
- `ui/screens/login/LoginScreen.kt:70` — `println()`
- `ui/screens/competitions/CompetitionsViewModelImpl.kt:71` — `println()`
- `ui/screens/results/ResultsViewModelImpl.kt:103` — `println()`
- `ui/screens/login/LoginViewModel.kt:94,98,103` — `println()`

## Fix
Remove all `println()` calls. Replace necessary logging with `Timber` or `Log` guarded by `BuildConfig.DEBUG`.
