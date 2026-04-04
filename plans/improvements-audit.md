# Webshooter App — Comprehensive Improvement Audit

**Date:** 2026-04-04
**Scope:** Full codebase review covering security, bugs, performance, UX, code quality, and architecture.

---

## Table of Contents

1. [Security](#1-security)
2. [Bug Risks](#2-bug-risks)
3. [Performance](#3-performance)
4. [UX & Accessibility](#4-ux--accessibility)
5. [State Management](#5-state-management)
6. [Code Quality](#6-code-quality)
7. [Architecture & Navigation](#7-architecture--navigation)
8. [Testing](#8-testing)
9. [Build & Configuration](#9-build--configuration)

---

## 1. Security

### CRITICAL — Hardcoded Client Secret
- **Files:** `data/login/LoginRepository.kt:32`, `data/login/remote/RefreshTokenRequest.kt:5`
- **Issue:** OAuth client_secret `REMOVED-CLIENT-SECRET` is hardcoded in source code, visible in decompiled APK and git history.
- **Fix:** Move to a secure server-side endpoint or at minimum to BuildConfig with obfuscation.

### CRITICAL — R8/ProGuard Minification Disabled
- **File:** `app/build.gradle.kts:68`
- **Issue:** `isMinifyEnabled = false` in the release build. All app code is fully readable when decompiled. Combined with the hardcoded secret above, this is a significant security gap.
- **Fix:** Enable `isMinifyEnabled = true` for release builds and add proper ProGuard keep rules.

### CRITICAL — ProGuard Rules Empty
- **File:** `app/proguard-rules.pro`
- **Issue:** Only default commented-out rules. No keep rules for Room entities, Retrofit models, Hilt, or serialization classes. Enabling minification without fixing this will cause runtime crashes.
- **Fix:** Add keep rules for data classes, Retrofit interfaces, Room entities, and Hilt components.

### HIGH — Mock Credentials in Production Build
- **File:** `ui/screens/login/LoginViewModel.kt:41-50`
- **Issue:** Hardcoded `mockuser`/`mockpassword` check exists in production. Anyone knowing these credentials can bypass login and enter mock mode.
- **Fix:** Guard behind `BuildConfig.DEBUG` or remove from release builds entirely.

### HIGH — HTTP Logging in Production
- **File:** `di/NetworkModule.kt:52`
- **Issue:** `HttpLoggingInterceptor` at `Level.BASIC` runs in release builds. May log tokens and sensitive headers.
- **Fix:** Conditionally set level to `NONE` when `!BuildConfig.DEBUG`.

### HIGH — Cleartext Traffic Permitted
- **File:** `res/xml/network_security_config.xml:3`
- **Issue:** Allows HTTP traffic to `webshooter.se` and `localhost`, enabling downgrade attacks.
- **Fix:** Remove cleartext permission for `webshooter.se`; keep localhost only for debug builds.

### MEDIUM — In-Memory Token Cache Without Volatile
- **File:** `data/AuthTokenManager.kt:101-103`
- **Issue:** Companion object vars `token`, `refreshToken`, `tokenExpiresAtMillis` are read/written from multiple threads without `@Volatile` or synchronization.
- **Fix:** Add `@Volatile` to all three fields, or synchronize access.

### MEDIUM — Cookie Domain Matching Too Loose
- **File:** `data/AuthCookieJar.kt:13`
- **Issue:** Uses `endsWith()` for domain comparison, which could match unrelated domains (e.g., `evil-webshooter.se`).
- **Fix:** Use strict domain equality or proper cookie domain matching per RFC 6265.

### MEDIUM — Hardcoded Browser User-Agent
- **File:** `data/GeneralHeadersInterceptor.kt:24-25`
- **Issue:** Spoofs Chrome/Windows User-Agent. Makes API traffic indistinguishable from browser traffic, may violate ToS, and complicates server-side analytics.
- **Fix:** Use a proper app-specific User-Agent string like `Webshooter-Android/1.10.0`.

### LOW — Cookie Values Potentially Logged
- **File:** `data/AuthCookieJar.kt:30-35`
- **Issue:** `getSessionCookies()` returns raw cookie values. If logged via HttpLoggingInterceptor, session tokens are exposed.
- **Fix:** Mask cookie values in any debug output.

---

## 2. Bug Risks

### HIGH — Race Condition on Token Refresh
- **File:** `data/AuthTokenManager.kt:101-103`
- **Issue:** Static mutable token variables accessed without synchronization. Thread A in AuthInterceptor may read a stale token while Thread B updates it in TokenAuthenticator.
- **Fix:** Use `@Volatile` or `AtomicReference`, or always read from EncryptedSharedPreferences.

### HIGH — Inconsistent competitionId Types (Int vs Long)
- **Files:** `data/results/ResultsRepository.kt:24` (Int), `data/competitionteams/CompetitionTeamsRepository.kt:24` (Long), `data/competitionpatrols/CompetitionPatrolsRepository.kt:24` (Long), `ui/navigation/AppNavHost.kt:66` (IntType) vs `:87` (LongType)
- **Issue:** Results module uses `Int` for competitionId while others use `Long`. Navigation routes also mix types. IDs exceeding `Int.MAX_VALUE` will silently overflow.
- **Fix:** Standardize all competitionId parameters to `Long` across the entire codebase.

### HIGH — Database Cache Race Condition
- **File:** `data/competitions/CompetitionsRepository.kt:85-86`
- **Issue:** `deleteAll()` then `insertAll()` without a Room `@Transaction`. A concurrent read between the two calls returns empty data.
- **Fix:** Wrap in a `@Transaction` method on the DAO.

### MEDIUM — Memory Leak in UserSessionProvider
- **File:** `data/UserSessionProvider.kt:20-30`
- **Issue:** `CoroutineScope(SupervisorJob() + Dispatchers.IO)` is created but never cancelled. Jobs accumulate over the app lifetime.
- **Fix:** Implement `Closeable` and cancel the scope, or tie to a lifecycle-aware scope.

### MEDIUM — Expired Cookies Never Cleaned
- **File:** `data/AuthCookieJar.kt`
- **Issue:** Cookies are stored indefinitely in a HashMap. `loadForRequest()` never checks `cookie.expiresAt`. Stale cookies accumulate.
- **Fix:** Filter out expired cookies in `loadForRequest()`.

### MEDIUM — Unsafe Non-Null Assertion
- **File:** `ui/screens/signup/SignupScreen.kt:129`
- **Issue:** `uiState.error!!` — will crash if error is null.
- **Fix:** Use safe call (`?.`) or `requireNotNull()` with a meaningful message.

### MEDIUM — Fallback Navigation Value -1L
- **File:** `ui/navigation/AppNavHost.kt:116`
- **Issue:** `arguments?.getLong("competitionId") ?: -1L` — if argument is missing, -1L is silently passed downstream causing confusing API errors.
- **Fix:** Throw an `IllegalArgumentException` or navigate back on missing required arguments.

### LOW — String "null" Handling
- **File:** `ui/screens/club/ClubScreen.kt:102-110`
- **Issue:** `.takeIf { it != "null" }` to handle the literal string "null" from the API. Fragile workaround.
- **Fix:** Fix the data model to use nullable types properly, or handle at the repository/mapper layer.

### LOW — Gson Deserialization Without Try-Catch
- **Files:** `data/results/local/ResultMappers.kt:36`, `data/competitions/local/CompetitionMappers.kt:91-108`, `data/mysignups/local/SignupMappers.kt:46-50`, `data/settings/local/UserProfileMappers.kt:45`
- **Issue:** `gson.fromJson()` can throw `JsonSyntaxException` if cached JSON is corrupted or schema changes. Not caught.
- **Fix:** Wrap in try-catch and return null/empty on failure, triggering a fresh network fetch.

---

## 3. Performance

### HIGH — Missing `key()` in Lazy Lists
- **Files:** `ui/screens/competitions/CompetitionsScreen.kt:193`, `ui/screens/signups/SignupsScreen.kt:124-144`
- **Issue:** `items(filteredData)` without explicit keys. When list order changes or items are added/removed, Compose may reuse composables incorrectly causing visual glitches or wasted recomposition.
- **Fix:** Add `key = { it.id }` to all `items()` and `itemsIndexed()` calls.

### MEDIUM — Unnecessary Recompositions
- **File:** `ui/screens/competitions/CompetitionsScreen.kt:94-105`
- **Issue:** `navigationItems` list recreated on every recomposition without `remember`.
- **File:** `ui/screens/results/ResultsScreen.kt:133-159`
- **Issue:** `currentUserIndices` recalculated every recomposition. Should use `derivedStateOf`.
- **Fix:** Wrap constant or derived values in `remember {}` or `derivedStateOf {}`.

### MEDIUM — Heavy Operations in Composition
- **File:** `ui/screens/results/ResultsScreen.kt:369-476`
- **Issue:** Large `forEach` loop building grouped results runs during composition, not in a side effect or ViewModel.
- **File:** `ui/screens/myresults/MyResultsScreen.kt:220-246`
- **Issue:** Complex grid calculation logic runs on every recompose.
- **Fix:** Move computation to ViewModel or wrap in `remember(dependencies)`.

### MEDIUM — Large Composables
- **File:** `ui/screens/competitions/CompetitionsScreen.kt:340-535`
- **Issue:** `CompetitionItem` is 196 lines with 4 Card components. Hard to maintain, causes broad recomposition scope.
- **File:** `ui/screens/results/ResultsScreen.kt:278-480`
- **Issue:** `ResultsList` is 203 lines mixing grouped and flat view logic.
- **Fix:** Extract into smaller, focused composable functions.

### LOW — Missing Database Indices
- **Files:** `data/competitionteams/local/TeamEntity.kt`, `data/competitionpatrols/local/PatrolEntity.kt`, `data/competitionsignups/local/CompetitionSignupEntity.kt`
- **Issue:** Queries filter by `competitionId` frequently but no `@Index` is defined, causing full table scans.
- **Fix:** Add `indices = [Index("competitionId")]` to `@Entity` annotations.

### LOW — Destructive Database Migration
- **File:** `di/DatabaseModule.kt:29`
- **Issue:** `.fallbackToDestructiveMigration()` deletes all cached data on any schema change.
- **Fix:** Implement proper Room migrations for production; only use destructive for debug builds.

---

## 4. UX & Accessibility

### HIGH — Missing Content Descriptions
- **Files:** `ui/screens/login/LoginScreen.kt:118`, `ui/screens/settings/SettingsScreen.kt:447`
- **Issue:** Password visibility toggle icons have `contentDescription = null`. Screen readers cannot describe these buttons.
- **File:** `ui/screens/signup/SignupScreen.kt:60`
- **Issue:** Back button has hardcoded `"Back"` instead of `stringResource`.
- **Fix:** Add proper `stringResource(R.string.xxx)` content descriptions to all interactive icons.

### HIGH — Missing Empty States
- **File:** `ui/screens/club/ClubScreen.kt:184-190`
- **Issue:** Member list shows spinner indefinitely when the list is empty — doesn't distinguish "loading" from "no members found."
- **File:** `ui/screens/results/ResultsScreen.kt:291-297`
- **Issue:** When no weapon groups are selected but groups exist, the entire content area is blank with no message. Users won't understand why nothing is shown.
- **Fix:** Add explicit empty state messages: "No members found", "No results match your filters", etc.

### MEDIUM — Hardcoded Swedish Strings
- **File:** `ui/screens/results/ResultsScreen.kt:548, 562-567`
- **Issue:** "Gruppering", "Vapenklass", "Klubb", "Medl", "Ingen" are hardcoded strings not going through `stringResource()`.
- **Fix:** Move all user-facing text to `strings.xml`.

### MEDIUM — Touch Targets Below 48dp Minimum
- **File:** `ui/screens/myresults/MyResultsScreen.kt:190`
- **Issue:** IconButton with `size(20.dp)` — well below the 48dp accessibility minimum for touch targets.
- **Fix:** Use `Modifier.size(48.dp)` for the touch target; visually size the icon smaller inside.

### MEDIUM — Hardcoded Year
- **File:** `ui/screens/settings/SettingsScreen.kt:336`
- **Issue:** `currentYear = 2026` is hardcoded. Will be wrong next year.
- **Fix:** Use `Calendar.getInstance().get(Calendar.YEAR)`.

### LOW — Small Loading Indicator
- **File:** `ui/screens/myresults/MyResultsScreen.kt:75-86`
- **Issue:** Loading spinner is 24.dp with no explanatory text. Easy to miss.
- **Fix:** Use standard loading indicator size and add "Loading..." text.

### LOW — No Dark Theme for XML Resources
- **File:** `res/values/themes.xml:4`
- **Issue:** XML theme only defines light. Compose theme handles dark mode, but splash screen and system bars may not follow.
- **Fix:** Add `values-night/themes.xml` for system-level dark theme consistency.

---

## 5. State Management

### HIGH — State Lost on Configuration Change
- **File:** `ui/screens/signups/SignupsScreen.kt:108-123`
- **Issue:** Local state (dropdown expanded, note text, selected weapon class) all use `remember` instead of `rememberSaveable`. Rotation destroys all user input.
- **File:** `ui/screens/results/ResultsScreen.kt:92-106`
- **Issue:** `isFilterBottomSheetOpen`, `secondsLeft`, `refreshTrigger`, `occurrenceIdx` not saved across config changes.
- **Fix:** Use `rememberSaveable` for all user-interactive state, or move state to ViewModel with `SavedStateHandle`.

### MEDIUM — Password Visibility Lost on Rotation
- **File:** `ui/screens/login/LoginScreen.kt:52-54`
- **Issue:** `passwordVisible` uses `remember` — rotation hides the password again mid-typing.
- **Fix:** Use `rememberSaveable`.

### MEDIUM — Navigation State Not Saved
- **File:** `ui/landingscreen/WebShooterScreen.kt:52-59`
- **Issue:** `selectedRoute` uses `remember`, not `rememberSaveable`. After rotation, the selected tab may not match the actual navigation state.
- **Fix:** Use `rememberSaveable` or derive from `navController.currentBackStackEntry`.

### LOW — Logout Dialog Lost on Rotation
- **File:** `ui/screens/settings/SettingsScreen.kt:131`
- **Issue:** `showLogoutDialog` is `remember`. If user opens the dialog and rotates, it disappears.
- **Fix:** Use `rememberSaveable`.

---

## 6. Code Quality

### HIGH — Debug Logging Left in Production
- **Files:**
  - `ui/screens/shooterresult/ShooterResultScreen.kt:50` — `Log.d()`
  - `ui/screens/login/LoginScreen.kt:70` — `println()`
  - `ui/screens/competitions/CompetitionsViewModelImpl.kt:71` — `println()`
  - `ui/screens/results/ResultsViewModelImpl.kt:103` — `println()`
  - `ui/screens/login/LoginViewModel.kt:94,98,103` — `println()`
- **Fix:** Remove all `println()` calls. Replace necessary logging with `Timber` or `Log` guarded by `BuildConfig.DEBUG`.

### HIGH — printStackTrace() Instead of Proper Logging
- **Files:** All repositories (`LoginRepository`, `CookiesRepository`, `ResultsRepository`, `CompetitionsRepository`, `CompetitionTeamsRepository`, `CompetitionPatrolsRepository`, `CompetitionSignupsRepository`, `SignupsRepository`)
- **Issue:** Exceptions caught with `printStackTrace()` — not captured by Crashlytics, exposes implementation details, not debuggable.
- **Fix:** Use `Log.e(TAG, message, exception)` or Timber.

### MEDIUM — Magic Numbers for Competition Types
- **Files:** `ui/screens/competitions/CompetitionsScreen.kt:196-198`, `ui/screens/patrols/PatrolsScreen.kt:58`
- **Issue:** `competitionType.id in setOf(2, 3, 9, 10)` repeated in multiple files with no explanation.
- **File:** `ui/screens/myresults/MyResultsScreen.kt:209`
- **Issue:** `isFalt = type == "Fält"` — hardcoded string comparison.
- **Fix:** Define named constants like `val PATROL_COMPETITION_TYPE_IDS = setOf(2, 3, 9, 10)` in a shared location.

### MEDIUM — Duplicated UI Patterns
- **Weapon class badge cards:** Duplicated in `PatrolsScreen.kt:142-214` and `SignupsScreen.kt:172-200`
- **Error/empty state handling:** Inconsistent across `MyResultsScreen`, `ClubScreen`, `PatrolsScreen`
- **Filter bottom sheet:** Similar structure in `ResultsScreen.kt:534-606` and `SignupsScreen.kt:203-298`
- **Fix:** Extract shared composables: `EmptyStateBox`, `ErrorStateCard`, `FilterBottomSheet`.

### MEDIUM — Repository Cache Pattern Duplication
- **Files:** All competition-related repositories duplicate ~50 lines of cache-remote-sync logic.
- **Issue:** `CachedResourceFlow.kt` utility exists but is unused.
- **Fix:** Refactor repositories to use the existing `cachedResourceFlow()` utility.

### LOW — Inconsistent ViewModel Injection
- **Issue:** Mix of `hiltViewModel()` and `hiltViewModel<ViewModelImpl>()` across screens.
- **Fix:** Pick one pattern and apply consistently.

### LOW — Inconsistent Error Mapping
- **Issue:** All repositories map to generic `UserError.IOError`, `UserError.HttpError`, `UserError.UnknownError` — no distinction between 400, 401, 403, 404, 500.
- **Fix:** Expand `UserError` to include HTTP status codes for specific error recovery.

### LOW — Hardcoded Host Header
- **File:** `data/GeneralHeadersInterceptor.kt:18`
- **Issue:** Host hardcoded to `"webshooter.se"`. If BASE_URL changes, requests break.
- **Fix:** Extract host from request URL dynamically.

---

## 7. Architecture & Navigation

### MEDIUM — No Deep Link Support
- **File:** `ui/navigation/Screen.kt`
- **Issue:** No `deepLinks` defined for any screen. App can't be opened to specific competitions from notifications or external links.
- **Fix:** Add deep link definitions for key screens (competition details, results).

### MEDIUM — Navigation State vs NavController Mismatch
- **File:** `ui/landingscreen/WebShooterScreen.kt:107-122`
- **Issue:** Manual route tracking via `selectedRoute` string. Hardware back button can desync this from NavController's actual back stack.
- **Fix:** Derive selected state from `navController.currentBackStackEntryAsState()`.

### MEDIUM — Cookies Not Persisted Across App Restarts
- **File:** `data/AuthCookieJar.kt`
- **Issue:** Cookies stored in an in-memory `MutableMap`. App restart loses all session cookies, requiring re-authentication.
- **Fix:** Persist cookies to SharedPreferences or database.

### LOW — Unsafe Navigation Argument Extraction
- **File:** `ui/navigation/AppNavHost.kt:113-115`
- **Issue:** `remember(backStackEntry)` without null check. `getBackStackEntry()` may return null.
- **Fix:** Add null safety checks.

---

## 8. Testing

### CRITICAL — Near-Zero Test Coverage
- **File:** `app/src/androidTest/.../StationResultRemoteDataSourceTest.kt` (76 lines, 1 test)
- **Issue:** The entire app has **one test file** with a single test method. No unit tests exist (`src/test/` is empty).
- **Recommended test additions:**
  - **Unit tests:** All ViewModels (state transitions, error handling), all Repositories (cache logic, error mapping), mappers (entity ↔ domain)
  - **Integration tests:** Auth flow (login → token storage → refresh), navigation (screen transitions, argument passing)
  - **UI tests:** Login screen, competition list, results display

---

## 9. Build & Configuration

### MEDIUM — Compose Compiler Version Mismatch
- **File:** `app/build.gradle.kts:110`
- **Issue:** `kotlinCompilerExtensionVersion = "1.4.3"` with Kotlin 2.0.20. These versions may not be compatible.
- **Fix:** Use version catalog alignment or the Compose compiler Gradle plugin.

### LOW — No Staging/Dev Build Flavors
- **File:** `app/build.gradle.kts:91`
- **Issue:** Only `prod` flavor exists. No way to test against staging APIs without code changes.
- **Fix:** Add `dev`/`staging` flavors with different BASE_URLs.

### LOW — SharedPreferences Apply vs Commit
- **File:** `data/AuthTokenManager.kt:46-64`
- **Issue:** `storeToken()` uses `.apply()` (async). Rapid writes from multiple threads may lose data.
- **Fix:** Use `.commit()` for critical auth data, or synchronize writes.

---

## Priority Summary

| Priority | Count | Examples |
|----------|-------|---------|
| **CRITICAL** | 4 | Hardcoded secrets, minification disabled, empty ProGuard, no tests |
| **HIGH** | 12 | Mock creds in prod, race conditions, type mismatches, debug logging, missing empty states, state loss on rotation |
| **MEDIUM** | 18 | Performance issues, hardcoded strings, code duplication, cookie handling, navigation sync |
| **LOW** | 12 | Inconsistent patterns, missing indices, small UI polish items |

**Total: 46 actionable items**
