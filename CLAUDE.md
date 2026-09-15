# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

### Android
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

### iOS

```bash
# Link the shared framework for the iOS Simulator (fastest sanity check).
# Auto-detects/installs the Kotlin/Native compiler on first run.
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-daemon

# Regenerate the Xcode project from iosApp/project.yml (no-op if unchanged).
(cd iosApp && xcodegen generate)

# Build the Staging scheme for an iPhone Simulator.
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme Staging -configuration Debug-Staging \
  -destination 'platform=iOS Simulator,name=iPhone 17' build

# Same, but switching to Prod backend.
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme Prod -configuration Debug-Prod \
  -destination 'platform=iOS Simulator,name=iPhone 17' build

# Install + launch a built .app on the booted simulator.
xcrun simctl install booted \
  ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-Staging-iphonesimulator/iosApp.app
xcrun simctl launch booted se.kjellstrand.webshooter
```

`iosApp/project.yml` is the source of truth; `iosApp/iosApp.xcodeproj/` is gitignored and regenerated from it via [xcodegen](https://github.com/yonaskolb/XcodeGen). Run `xcodegen generate` after editing `project.yml`.

**Environment gotchas (Mac):**
- Gradle 8.9 cannot run under JDK 26 (max supported is JDK 22). Use OpenJDK 17–22 — install via `brew install openjdk@21` and set `JAVA_HOME` to `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`. The Xcode Run Script in `project.yml` auto-detects an installed `openjdk@N` if `JAVA_HOME` isn't set.
- If `xcode-select -p` points at `/Library/Developer/CommandLineTools`, the iOS link will fail. Either `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer` (system change) or set `DEVELOPER_DIR` inline for the build.
- `gradlew` is currently tracked in git without the executable bit (mode `100644`). `./gradlew` from the Xcode Run Script fails with "Permission denied" — that's why the script invokes it via `sh ./gradlew`. Fix permanently with `git update-index --chmod=+x gradlew`.

On Windows, run gradle via the shell script (`./gradlew`) not `gradlew.bat` directly. iOS Kotlin/Native targets (`compileKotlinIosX64`, `linkDebugFrameworkIosX64`, etc.) silently SKIP on Windows — they require a macOS host.

### iOS — DebugGallery (smoke-test screens without backend)

`shared/src/iosMain/.../ui/DebugGallery.kt` lets you render any single screen on Simulator using its mock VM, by launching the app with the `SCREEN` env var:

```bash
xcrun simctl terminate booted se.kjellstrand.webshooter 2>/dev/null
SIMCTL_CHILD_SCREEN=competitions xcrun simctl launch booted se.kjellstrand.webshooter
```

Known screen names: `login, competitions, myentries, charts, club, settings, licenses, results, shooter, signupslist, patrols, teams, signup`. When `SCREEN` is unset, `MainViewController` boots the real `AppNavHost`. `ClubStats` and `SeriesPoints` aren't in the gallery — their screens take concrete `*Impl` VMs and no `ui/mock/` stubs exist; smoke-test those interactively against the backend.

## Module Layout

Two Gradle modules plus an Xcode project:

- **`:shared`** — Kotlin Multiplatform (`commonMain`, `androidMain`, `iosMain`). Contains the entire data layer (repositories, Ktor data sources, Room entities/DAOs, `AppDatabase`), the Koin DI graph, the platform factories (`HttpClient`, `AppDatabase`, `AuthTokenManager`, `SecurePrefs`), **and the entire Compose UI** — every screen (`ui/screens/<feature>/XxxScreen.kt`), ViewModel + UI state, the theme (`ui/theme/`), shared composables (`ui/common/`), the Compose-preview mocks (`ui/mock/`), the platform abstractions (`ui/platform/` including a multiplatform `BackHandler` expect/actual), and the **top-level navigation graph** (`ui/navigation/AppNavHost.kt`, `Screen.kt`, `NavigationArguments.kt`, `NavControllerExtensions.kt`) and the **landing/drawer scaffold** (`ui/landingscreen/WebShooterScreen.kt`). The UI is built with **Compose Multiplatform** (JetBrains `compose` plugin), so it is KMP-portable. Navigation uses the JetBrains multiplatform port `org.jetbrains.androidx.navigation:navigation-compose` (NOT the Android-only `androidx.navigation:navigation-compose`). The `Shared.framework` binary is configured for all three iOS targets.
- **`:app`** — thin Android host. Contains only what must be Android-specific: `MainActivity` (boots the Compose hierarchy and wires `showMessage` to `Toast`), `ShooterApplication` (Koin init, Napier, Firebase), the `MockInterceptor` (reads `R.raw.*`). Android XML themes live in `app/src/main/res/values{,-night}/themes.xml` (for the system splash / Activity chrome — distinct from the Compose theme in `:shared`).
- **`iosApp/`** — SwiftUI host generated from `project.yml` by [xcodegen](https://github.com/yonaskolb/XcodeGen). Contains `WebshooterApp.swift` (the `@main` SwiftUI App that builds a `WebshooterConfig` from `Bundle.main.infoDictionary` and calls `KoinKt.doInitKoin`), `ContentView.swift` (a `UIViewControllerRepresentable` wrapper for the shared `MainViewController`), `Info.plist`, `iosApp.entitlements` (declares `keychain-access-groups` so `AuthTokenManager` and `SecurePrefs` can write to Keychain — required even on Simulator), and minimal `Assets.xcassets/` (AppIcon, AccentColor). The Xcode `.xcodeproj/` is gitignored and regenerated from `project.yml`.

Compose resources (strings, drawables) are shared: they live in `shared/src/commonMain/composeResources/` and are accessed via the generated `se.kjellstrand.webshooter.resources.Res` (configured `publicResClass = true`, custom package in `:shared/build.gradle.kts`). Use `stringResource(Res.string.foo)` from `org.jetbrains.compose.resources`, not Android `R.string`.

### iOS host wiring (`iosApp/` + `:shared/iosMain`)

- `shared/src/iosMain/.../ui/MainViewController.kt` is the iOS entry point. Returns a `ComposeUIViewController { WebShooterTheme { AppNavHost(rememberNavController(), showMessage = …) } }`. When the launcher sets the `SCREEN` env var (`SIMCTL_CHILD_SCREEN=<name>`), it routes through `DebugGallery` instead — see the iOS Build Commands section.
- `IosPlatformModule.iosPlatformModule(config: WebshooterConfig)` provides every iOS-side binding to Koin: Darwin Ktor `HttpClient`, Keychain-backed `AuthTokenManager` + `SecurePrefs`, Documents-directory Room database, `IosUrlLauncher` (uses `UIApplication.openURL`), and `IosCalendarOpener` (uses EventKit's `EKEventStore.saveEvent`, requires `NSCalendarsUsageDescription` in `Info.plist`).
- `xcodegen` defines two schemes — **Staging** (hits `staging.webshooter.se`) and **Prod** (hits `webshooter.se`) — by setting `WEBSHOOTER_BASE_URL` as a per-configuration build setting that's injected into `Info.plist` via `$(WEBSHOOTER_BASE_URL)` substitution. The Swift side reads it through `Bundle.main.infoDictionary["WebshooterBaseUrl"]`. `WEBSHOOTER_CLIENT_SECRET` follows the same pattern, but its value comes from the git-ignored `iosApp/Secrets.xcconfig` (included by `iosApp/Base.xcconfig`). On Android it comes from `CLIENT_SECRET` in `local.properties` (or env `WEBSHOOTER_CLIENT_SECRET`). The repo is public: never commit the secret; `HardcodedSecretTest` guards this.
- Simulator builds use **ad-hoc code signing** (`CODE_SIGN_IDENTITY = -` + the entitlements file) because Keychain access on iOS requires entitlements to be embedded into the `.app`, even on Simulator. Real team signing comes later when targeting devices.
- The Xcode "Run Script" build phase invokes `:shared:embedAndSignAppleFrameworkForXcode`. It auto-locates a usable JDK (17–22) and explicitly passes `KOTLIN_FRAMEWORK_BUILD_TYPE` so the plugin's auto-detect handles the `Debug-Staging` / `Release-Prod` config names.
- iOS-only Compose Multiplatform gotcha: `Info.plist` must include `CADisableMinimumFrameDurationOnPhone = true` or `androidx.compose.ui.uikit.PlistSanityCheck` throws `SIGABRT` on startup.

## Architecture

Clean three-layer architecture: **data → di (Koin) → ui (screens + view-models + navigation)**, all living in `:shared`. `:app` is a thin Android host that boots Koin and renders the shared `AppNavHost`; `iosApp/` is a thin SwiftUI host that does the same via `ComposeUIViewController`. Each feature is a vertical slice across the layers.

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

**Navigation is two-level** (Compose Multiplatform Navigation; routes are a sealed class in `:shared/commonMain/.../ui/navigation/Screen.kt`):
- The **top-level `AppNavHost`** (`:shared`) starts at `Screen.SplashScreen`. `SplashScreen` checks for a stored token and routes to either `LoginScreen` or `LandingScreen`. `AppNavHost` also owns the full-screen *detail* destinations (competition results, shooter result, signup, signups list, patrols, teams) and the session-expiry redirect driven by `SessionManager.events`. Takes a `showMessage: (String) -> Unit` parameter — Android `MainActivity` wires it to `Toast`, iOS `MainViewController` wires it to a `Napier` log placeholder for now.
- The **`WebShooterScreen`** landing scaffold (`:shared/.../ui/landingscreen/`) hosts its own *nested* `NavHost` behind a `ModalNavigationDrawer`, starting at `CompetitionsList`. The drawer sections (Competitions, MyEntries, Charts, SeriesPoints, ClubStats, Club, Settings, Licenses) are nested destinations, not top-level routes.

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

## iOS port — current status

Working on Simulator end-to-end as of commit `77aa688`:
- iOS host (`iosApp/`) launches, boots Koin with a per-scheme backend URL, renders the shared `AppNavHost` (Splash → Login → Landing → drawer).
- All 13 mock-driven screens reachable via `DebugGallery` render without crash on iPhone 17 Simulator (iOS 26.5 SDK).
- Keychain, Darwin Ktor, Room (Documents directory), shared compose-resources, EventKit calendar add, multiplatform `BackHandler` — all wired.

Not yet verified / still TODO:
- **Interactive walk-through against a real backend.** Type real credentials into `LoginScreen`, navigate every drawer section + every detail screen, watch for surprises (date locale, focus, scroll). Staging DNS isn't reachable from at least one network I tried; if you hit the same wall, switch to the Prod scheme.
- **`ClubStatsScreen` and `SeriesPointsScreen`** aren't in `DebugGallery` (their screens take concrete `*Impl` VMs and no `ui/mock/` stubs exist). Verify them via the real flow.
- **`IosCalendarOpener`** is implemented but never exercised end-to-end — try "add to calendar" from a competition results screen.
- **LaunchScreen polish:** currently an empty `UILaunchScreen` dict in `Info.plist`. A branded launch with the icon centred would be nicer.
- **Device install + TestFlight:** plan Phase 8 and 9, both deferred. Needs your Apple ID for personal-team signing.
- **Firebase Crashlytics/Analytics:** plan Phase 10, deferred per the original decision.
- **`gradlew` git mode is still `100644`.** Fix with `git update-index --chmod=+x gradlew` to drop the `sh ./gradlew` workaround in the Xcode Run Script.
