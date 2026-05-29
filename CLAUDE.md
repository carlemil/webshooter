# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Compile check (fastest feedback)
./gradlew :app:compileProdReleaseSources --no-daemon

# Compile shared module (commonMain + androidMain)
./gradlew :shared:compileDebugKotlinAndroid --no-daemon

# Compile commonMain only (KMP-portable code)
./gradlew :shared:compileCommonMainKotlinMetadata --no-daemon

# Full build
./gradlew :app:build --no-daemon

# Assemble release APK / bundle
./gradlew :app:assembleProdRelease --no-daemon

# Unit tests (JVM, Robolectric where applicable)
./gradlew :app:testProdReleaseUnitTest --no-daemon

# Single unit test
./gradlew :app:testProdReleaseUnitTest --tests "se.kjellstrand.webshooter.data.SomeTest" --no-daemon

# Instrumented tests
./gradlew :app:connectedAndroidTest --no-daemon
```

On Windows, run gradle via the shell script (`./gradlew`) not `gradlew.bat` directly. iOS Kotlin/Native targets (`compileKotlinIosX64`, `linkDebugFrameworkIosX64`, etc.) silently SKIP on Windows — they require a macOS host.

## Module Layout

Two Gradle modules:

- **`:shared`** — Kotlin Multiplatform (`commonMain`, `androidMain`, `iosMain`). Contains the entire data layer (repositories, Ktor data sources, Room entities/DAOs, `AppDatabase`), the Koin DI graph, the platform factories (`HttpClient`, `AppDatabase`, `AuthTokenManager`, `SecurePrefs`), **and the entire Compose UI** — every screen (`ui/screens/<feature>/XxxScreen.kt`), ViewModel + UI state, the theme (`ui/theme/`), shared composables (`ui/common/`), the Compose-preview mocks (`ui/mock/`), and the platform abstractions (`ui/platform/`). The UI is built with **Compose Multiplatform** (JetBrains `compose` plugin), so it is KMP-portable. The `Shared.framework` binary is configured for all three iOS targets.
- **`:app`** — thin Android host. Contains only what must be Android-specific: `MainActivity`, `ShooterApplication` (Koin init, Napier, Firebase), the `MockInterceptor` (reads `R.raw.*`), the top-level navigation graph (`ui/navigation/` — `Screen.kt`, `AppNavHost.kt`, `NavControllerExtensions.kt`, `NavigationArguments.kt`), and the `WebShooterScreen` landing/drawer scaffold (`ui/landingscreen/`). Android XML themes live in `app/src/main/res/values{,-night}/themes.xml` (for the system splash / Activity chrome — distinct from the Compose theme in `:shared`).

Compose resources (strings, drawables) are shared: they live in `shared/src/commonMain/composeResources/` and are accessed via the generated `se.kjellstrand.webshooter.resources.Res` (configured `publicResClass = true`, custom package in `:shared/build.gradle.kts`). Use `stringResource(Res.string.foo)` from `org.jetbrains.compose.resources`, not Android `R.string`.

iOS does not yet have its own host app, but `:shared/iosMain` is wired (Darwin Ktor engine, Keychain-backed settings, Documents-directory Room database, iOS Koin platform module, `Ios*` platform-abstraction impls) so that adding one is mostly Swift glue + an `initKoin(iosPlatformModule(config))` call.

## Architecture

Clean three-layer architecture: **data → di (Koin) → ui (screens + view-models)**, all living in `:shared`. `:app` is just the Android host that boots Koin and wires the navigation graph. Each feature is a vertical slice across the layers.

### Data Layer (`:shared/commonMain/data/<feature>/`)
Each feature has:
- `remote/` — `XxxRemoteDataSource` interface + `XxxRemoteDataSourceKtor` implementation (Ktor calls returning serializable response DTOs).
- `local/` — Room `XxxEntity` + `XxxDao` + mapping extensions (`toDomain`, `toEntity`, `contentHash`).
- `XxxRepository.kt` — Flow-emitting wrapper that observes the DAO, syncs from the remote, and writes back.

All async results are wrapped in `Resource<T, E>` (sealed interface: `Loading`, `Success`, `Error`) from `data/common/Resource.kt`. UI is offline-first: every flow starts with `Resource.Loading` so screens never flash between loading and error states.

### Networking (Ktor, multiplatform)
`HttpClientFactory.kt` (commonMain) defines `configureWebshooterHttpClient`, applied by per-platform `createWebshooterHttpClient` factories — OkHttp engine on Android, Darwin engine on iOS. Plugins installed:

- **`Auth.bearer`** — reads tokens from `AuthTokenManager`, auto-refreshes via `POST api/v4.1.9/oauth/token`, and emits `SessionManager.SessionEvent.Expired` when refresh fails. The token is **never** sent to the OAuth token endpoint itself (would cause a 500).
- **`HttpCookies`** with `AcceptAllCookiesStorage` — replaces the old `AuthCookieJar`.
- **`Logging`** — `LogLevel.ALL` on debug builds (intentional, do not downgrade), `NONE` on release. Routes through Napier so logs land in logcat / iOS log system.
- **`ContentNegotiation`** with the shared `Json` (see below).
- **`DefaultRequest`** — base URL plus the headers the webshooter.se backend expects (`Accept`, `Accept-Language`, `Referer`, `User-Agent`, `X-Requested-With`).

`expectSuccess = true`, so 4xx/5xx throw and are translated to `Resource.Error` inside repositories.

### `MockInterceptor` (Android-only)
Lives in `:app/data/MockInterceptor.kt` because it reads canned JSON from `R.raw.*`. It's wired into the Android Ktor engine via the `extraOkHttpInterceptors` parameter on `androidPlatformModule(...)`. Toggled by `MockModeManager.isMockMode`, which `LoginViewModelImpl` flips when the user logs in with the `mockuser` / `mockpassword` credentials reserved for Google Play reviewers.

### DI (Koin, no Hilt)
- `shared/commonMain/di/SharedModule.kt` — `sharedModule` declares every platform-agnostic singleton (`Json`, `SessionManager`, application `CoroutineScope` under qualifier `ApplicationCoroutineScopeQualifier`, all `*RemoteDataSource` impls, all 14 repositories, all 8 DAO accessors).
- `shared/commonMain/di/ViewModelsModule.kt` — `viewModelsModule` declares every ViewModel via Koin's `viewModel { ... }` DSL. The 5 VMs that previously took a `SavedStateHandle` (Teams, Signups, Patrols, Results, ShooterResult) now take their nav args as plain constructor params; `AppNavHost` extracts them from `NavBackStackEntry.arguments` and passes them via `koinViewModel { parametersOf(...) }`. The `at<T>(index)` extension on `ParametersHolder` is a typed positional helper since Koin 4.0.4 doesn't expose `get<T>(Int)`.
- `shared/{androidMain,iosMain}/di/*PlatformModule.kt` — supply platform-bound singletons (`HttpClient`, `AppDatabase`, `AuthTokenManager`, `SecurePrefs`, `WebshooterConfig`).
- `initKoin(platformModule)` (in `shared/commonMain/di/Koin.kt`) is called from `ShooterApplication.onCreate` **before** `super.onCreate()`. It registers `sharedModule + viewModelsModule + platformModule`. Anything trying to access Koin before that point will fail.
- Compose screens acquire VMs with `koinViewModel<XxxViewModelImpl>()` from `org.koin.compose.viewmodel`. Built on top of the KMP `androidx.lifecycle:lifecycle-viewmodel:2.8.6` artifact (the base `ViewModel` class works on both Android and iOS).

When you add a new repository or ViewModel, register it in `sharedModule` / `viewModelsModule` respectively. There is no Hilt module to also update — Koin runtime resolution is the only path.

### Database (Room 2.7, multiplatform)
`AppDatabase` is declared in `:shared/commonMain/data/db/AppDatabase.kt`. The Room KSP plugin is configured for `kspAndroid`, `kspIosX64`, `kspIosArm64`, and `kspIosSimulatorArm64` in `:shared/build.gradle.kts`. Platform `createAppDatabase()` factories live in `:shared/<platform>Main/data/db/`. Schemas are exported to `:shared/schemas/`; **`DB_VERSION` (in `data/db/DbVersion.kt`) and `appVersionCode` (in `app/build.gradle.kts`) are kept in sync** so the prebuilt-database asset regeneration keys correctly.

The Android `createAppDatabase` wraps `.createFromAsset(...)` in a try/catch: if the bundled `assets/databases/webshooter.db` schema doesn't match `@Database` (typical when `DB_VERSION` was bumped without regenerating the asset), it deletes the seeded file and rebuilds empty. Next sync rehydrates from the network — no user data loss because the cache is always rebuildable.

The `noResults` boolean column on `CompetitionEntity` is a skip-flag for competitions whose results endpoint persistently returns 5xx (a backend quirk for competitions with no results yet). `ResultsRepository.refreshResultsFor` returns a `RefreshOutcome` enum; `CompetitionsRepository.syncAll` flips `noResults` on `ServerError` so the next sync skips that competition until either a `Success` clears the flag or the row's `contentHash()` changes.

### Auth & Secrets
- `AuthTokenManager` — wraps `com.russhwolf.settings.Settings`. Backed by `EncryptedSharedPreferences` on Android (`createAuthTokenManager(context)`), Keychain on iOS (`createAuthTokenManager()` with `@OptIn(ExperimentalSettingsImplementation::class)`).
- `SecurePrefs` — same pattern, used for the saved username and other non-token credentials.
- `WebshooterConfig` — `isDebug`, `baseUrl`, `versionName`, `clientSecret`, supplied by the platform module from `BuildConfig` on Android.

### UI Layer
Each feature folder under `:shared/commonMain/ui/screens/<feature>/` holds the Compose **screen** (`XxxScreen.kt`), the **ViewModel** (interface `XxxViewModel` + `XxxViewModelImpl`), and the **UI state** (`XxxUiState`). ViewModels are plain `androidx.lifecycle.ViewModel` subclasses (the KMP artifact); they hold a `MutableStateFlow<UiState>` and screens observe with `collectAsState()`. Mock ViewModel/data implementations in `:shared/commonMain/ui/mock/` drive `@Preview`s (Android-only previews, but the code is shared).

**Platform abstractions** (`:shared/commonMain/ui/platform/`): `UrlLauncher` and `CalendarOpener` are `interface`s with Android/iOS impls (`AndroidUrlLauncher`, `IosUrlLauncher`, …) supplied by the platform Koin module and obtained in composables via `koinInject()`. Use these instead of touching Android `Intent`/`Context` directly so the screen stays KMP-portable.

**Navigation is two-level** (Jetpack/Compose Navigation; routes are a sealed class in `:app/ui/navigation/Screen.kt`):
- The **top-level `AppNavHost`** (`:app`) starts at `Screen.SplashScreen`. `SplashScreen` checks for a stored token and routes to either `LoginScreen` or `LandingScreen`. `AppNavHost` also owns the full-screen *detail* destinations (competition results, shooter result, signup, signups list, patrols, teams) and the session-expiry redirect driven by `SessionManager.events`.
- The **`WebShooterScreen`** landing scaffold (`:app/ui/landingscreen/`) hosts its own *nested* `NavHost` behind a `ModalNavigationDrawer`, starting at `CompetitionsList`. The drawer sections (Competitions, MyEntries, Charts, SeriesPoints, ClubStats, Club, Settings, Licenses) are nested destinations, not top-level routes.

Deep links use the base URI `https://webshooter.se/app`. The 5 nav-arg VMs (Teams, Signups, Patrols, Results, ShooterResult) receive their args via `koinViewModel { parametersOf(...) }` — see the DI section.

### Charts (pure Compose Canvas)
The 3 chart screens — all under `:shared/commonMain/ui/screens/charts/` — are `clubstats/ClubStatsScreen` (scatter), `seriespoints/SeriesPointsScreen` (multi-series line), and `resulttrends/` (the `ChartsScreen` composable in `ResultsTrendsScreen.kt`, combined scatter + trend). They are hand-rolled on top of `androidx.compose.foundation.Canvas`. There is **no third-party chart library** — the screens manage their own axes, grid, tick labels, hit-testing (`pointerInput { detectTapGestures }` finding the nearest point by Manhattan distance), and tooltip overlays (positioned via custom `Modifier.layout`).

Shared chart primitives in `:shared/commonMain/ui/common/`:
- `ChartStyles.kt` — `ChartShape` enum (Circle, Square, Triangle, Cross, X, ChevronDown, ChevronUp), `CHART_COLORS` palette, `DrawScope.drawScatterShape(shape, color, center, size)`.
- `ChartLegend.kt` — Compose `FlowRow` legend driven by `UserLegendItem(label, color, shapeIndex, id)`. `shapeIndex` 0..6 indexes into `ChartShape`; 7 = horizontal line (average), 8 = sloped line (trend).

Highlight state is hoisted to the screen (`highlightedLegendId: String?`) and shared between the chart and legend — tapping either dims everything that doesn't match. The chart code is KMP-portable (works on iOS via Compose Multiplatform) — it only depends on `androidx.compose.foundation.Canvas` and `androidx.compose.ui` primitives.

## Key Conventions

- **Product flavors:** `prod` (`https://webshooter.se/`) and `staging` (`https://staging.webshooter.se/`, applicationIdSuffix `.staging`). There is no separate mock flavor — mocking is a runtime toggle via `MockModeManager`.
- **API base path:** `api/v4.1.9/`.
- **JSON config:** `Json { ignoreUnknownKeys = true; coerceInputValues = true; explicitNulls = false; encodeDefaults = true }`. `encodeDefaults = true` is load-bearing — the OAuth endpoint requires `client_id` and `grant_type` which use defaults in `LoginRequest`. `coerceInputValues = true` plus `= 0L` defaults on data-class fields turns server-side `null`s on numeric fields (e.g. `StationResult.points`) into `0L` instead of crashing the whole payload.
- **Logging:** Napier (`io.github.aakira.napier`). `Napier.d/w/e(message, throwable, tag)`. `DebugAntilog` is registered in `ShooterApplication.onCreate` for debug builds only.
- **Pagination:** Repositories accept `page` and `perPage` params; ViewModels chain pages by calling `loadPage(next)` in the `Resource.Success` handler.
- **Current user identity:** Fetched via `SettingsRepository.getUserProfile()` which calls `authenticate/user`. Inject `SettingsRepository` when a screen needs the logged-in user's ID or name.
- **Highlighted state in lists:** `isCurrentUser` rows use `MaterialTheme.colorScheme.primary` background (same green as active `WeaponClassBadge`); non-highlighted rows use `Color.Unspecified` to fall back to defaults.
- **Column weights in patrol/signup lists:** header and data rows must share identical weight values to align columns.
- **Error handling:** Network errors are surfaced via toast and Napier-logged; UI remains responsive. Session expiry from token-refresh failure flows through `SessionManager.events` and is observed by `SessionViewModel` to bounce the user back to login.
- **KMP-only stdlib gotchas in commonMain:** `Dispatchers.IO` doesn't exist (use `Dispatchers.Default`); `@Volatile` is JVM-only (use `kotlin.concurrent.Volatile`); `java.time.*` is JVM-only (use `kotlinx-datetime`); `Map.toSortedMap` is JVM-only (sort entries manually).
