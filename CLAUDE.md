# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Compile check (fastest feedback)
./gradlew :app:compileProdReleaseSources --no-daemon

# Compile shared module (commonMain + androidMain)
./gradlew :shared:compileDebugKotlinAndroid --no-daemon

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

On Windows, run gradle via the shell script (`./gradlew`) not `gradlew.bat` directly.

## Module Layout

Two Gradle modules:

- **`:shared`** — Kotlin Multiplatform (`androidMain`, `iosMain`, `commonMain`). Contains the entire data layer (repositories, Ktor data sources, Room entities/DAOs, `AppDatabase`), the Koin DI graph, and platform factories for `HttpClient`, `AppDatabase`, `AuthTokenManager`, and `SecurePrefs`.
- **`:app`** — Android Compose application. Contains UI (screens, ViewModels, navigation, theme), `ShooterApplication`, the `MockInterceptor`, and a single `KoinHiltBridge` Hilt module.

iOS does not yet have its own host app, but `:shared/iosMain` is wired (Darwin Ktor engine, Keychain-backed settings, Documents-directory Room database) so that adding one is mostly Swift glue.

## Architecture

Clean three-layer architecture: **data (`:shared`) → di (Koin in `:shared`, Hilt bridge in `:app`) → ui (`:app`)**. Each feature is a vertical slice across the layers.

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

### DI (Koin + Hilt bridge)
- `:shared/commonMain/di/SharedModule.kt` defines `sharedModule` — every platform-agnostic singleton (Json, SessionManager, DAOs, remote data sources, repositories, application `CoroutineScope`).
- `:shared/androidMain/di/AndroidPlatformModule.kt` and `:shared/iosMain/di/IosPlatformModule.kt` supply platform-bound singletons (`HttpClient`, `AppDatabase`, `AuthTokenManager`, `SecurePrefs`, `WebshooterConfig`).
- `initKoin(platformModule)` is called from `ShooterApplication.onCreate` **before** `super.onCreate()`, so any Hilt-injected ViewModel constructed afterwards can resolve its dependencies via the bridge.
- `:app/di/KoinHiltBridge.kt` is the single remaining Hilt `@Module` in `:app`. Every `@Provides` is a one-liner that delegates to `KoinPlatform.getKoin().get()`. This lets ViewModels keep using `@HiltViewModel` constructor injection while the actual graph lives in Koin.

When you add a new repository, register it in `sharedModule`, then add a one-line `@Provides` in `KoinHiltBridge` so Hilt-injected ViewModels can pick it up.

### Database (Room 2.7, multiplatform)
`AppDatabase` is declared in `:shared/commonMain/data/db/AppDatabase.kt`. The Room KSP plugin is configured for `kspAndroid`, `kspIosX64`, `kspIosArm64`, and `kspIosSimulatorArm64` in `:shared/build.gradle.kts`. Platform `createAppDatabase()` factories live in `:shared/<platform>Main/data/db/`. Schemas are exported to `:shared/schemas/`.

### Auth & Secrets
- `AuthTokenManager` — wraps `com.russhwolf.settings.Settings`. Backed by `EncryptedSharedPreferences` on Android (`createAuthTokenManager(context)`), Keychain on iOS (`createAuthTokenManager()` with `@OptIn(ExperimentalSettingsImplementation::class)`).
- `SecurePrefs` — same pattern, used for the saved username and other non-token credentials.
- `WebshooterConfig` — `isDebug`, `baseUrl`, `versionName`, `clientSecret`, supplied by the platform module from `BuildConfig` on Android.

### UI Layer (`:app/ui/`)
MVI-like pattern: each screen has a `UiState` data class, a `ViewModel` interface, and a `ViewModelImpl` annotated `@HiltViewModel` that holds a `MutableStateFlow<UiState>`. Screens observe with `collectAsState()`. Mock implementations live in `:app/ui/mock/` and are used by Compose previews.

Navigation is Jetpack Compose Navigation. Routes are defined as a sealed class in `ui/navigation/Screen.kt` and wired in `ui/navigation/AppNavHost.kt`. Deep links use the base URI `https://webshooter.se/app`. Start destination is `LoginScreen`.

## Key Conventions

- **Product flavors:** `prod` (`https://webshooter.se/`) and `staging` (`https://staging.webshooter.se/`, applicationIdSuffix `.staging`). There is no separate mock flavor — mocking is a runtime toggle via `MockModeManager`.
- **API base path:** `api/v4.1.9/`.
- **JSON config:** `Json { ignoreUnknownKeys = true; coerceInputValues = true; explicitNulls = false; encodeDefaults = true }`. `encodeDefaults = true` is load-bearing — the OAuth endpoint requires `client_id` and `grant_type` which use defaults in `LoginRequest`.
- **Logging:** Napier (`io.github.aakira.napier`). `Napier.d/w/e(message, throwable, tag)`. `DebugAntilog` is registered in `ShooterApplication.onCreate` for debug builds only.
- **Pagination:** Repositories accept `page` and `perPage` params; ViewModels chain pages by calling `loadPage(next)` in the `Resource.Success` handler.
- **Current user identity:** Fetched via `SettingsRepository.getUserProfile()` which calls `authenticate/user`. Inject `SettingsRepository` when a screen needs the logged-in user's ID or name.
- **Highlighted state in lists:** `isCurrentUser` rows use `MaterialTheme.colorScheme.primary` background (same green as active `WeaponClassBadge`); non-highlighted rows use `Color.Unspecified` to fall back to defaults.
- **Column weights in patrol/signup lists:** header and data rows must share identical weight values to align columns.
- **Error handling:** Network errors are surfaced via toast and Napier-logged; UI remains responsive. Session expiry from token-refresh failure flows through `SessionManager.events` and is observed by `SessionViewModel` to bounce the user back to login.
