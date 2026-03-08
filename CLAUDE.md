# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Compile check (fastest feedback)
./gradlew :app:compileProdReleaseSources --no-daemon

# Full build
./gradlew :app:build --no-daemon

# Assemble release APK
./gradlew :app:assembleProdRelease --no-daemon

# Run instrumented tests
./gradlew :app:connectedAndroidTest --no-daemon
```

On Windows, run gradle via the shell script (`./gradlew`) not `gradlew.bat` directly.

## Architecture

Clean three-layer architecture: **data → di → ui**, feature-modular within each layer.

### Data Layer
Each feature has: `remote/Response.kt` (data classes) → `remote/XxxRemoteDataSource.kt` (Retrofit calls) → `XxxRepository.kt` (Flow-emitting wrapper).

All async results are wrapped in `Resource<T, E>` (sealed interface: `Loading`, `Success`, `Error`) from `data/common/Resource.kt`.

### DI Layer
One Hilt `@Module` per feature in `di/`, all installed in `SingletonComponent`. `NetworkModule` wires up OkHttp with interceptors: `GeneralHeadersInterceptor`, `AuthInterceptor`, `CookieHeadersInterceptor`, `HttpLoggingInterceptor` (level BASIC), and `MockInterceptor`.

### UI Layer
MVI-like pattern: each screen has a `UiState` data class, a `ViewModel` interface, and a `ViewModelImpl` (`@HiltViewModel`) that holds a `MutableStateFlow<UiState>`. Screens observe with `collectAsState()`.

Navigation is Jetpack Compose Navigation. Routes are defined as a sealed class in `ui/navigation/Screen.kt` and wired in `ui/navigation/AppNavHost.kt`. The start destination is `LoginScreen`.

### Auth Flow
- OAuth token stored via `AuthTokenManager` (EncryptedSharedPreferences)
- `AuthInterceptor` attaches the token to every request
- `AuthCookieJar` persists session cookies across requests
- Credentials stored in `data/secure/SecurePrefs.kt`

## Key Conventions

- **Product flavor:** Only `prod` exists (base URL: `https://webshooter.se/`). There is no mock flavor.
- **API base path:** `api/v4.1.9/`
- **Pagination:** Repositories accept `page` and `perPage` params; ViewModels chain pages automatically by calling `loadPage(next)` in the `Resource.Success` handler.
- **Current user identity:** Fetched via `SettingsRepository.getUserProfile()` which calls `authenticate/user`. Inject `SettingsRepository` when a screen needs the logged-in user's ID or name.
- **Highlighted state in lists:** `isCurrentUser` rows use `MaterialTheme.colorScheme.primary` background (same green as active `WeaponClassBadge`). Non-highlighted rows use `Color.Unspecified` to fall back to defaults.
- **Column weights in patrol/signup lists:** header and data rows must share identical weight values to align columns.
