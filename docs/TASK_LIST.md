# Implementation Task List

## Phase 1: Project Setup & Scaffolding
- [x] Initialize new Android Project (Kotlin, Jetpack Compose).
- [x] Configure `build.gradle.kts` (Module & Project level).
    - [x] Add Hilt dependencies (Dagger Hilt).
    - [x] Add Retrofit & OkHttp dependencies.
    - [x] Add Jetpack DataStore dependencies.
    - [x] Add Google Maps Compose dependencies.
    - [x] Add Navigation Compose dependencies.
- [x] Set up `AndroidManifest.xml`.
    - [x] Add permissions: `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`.
    - [x] Define `LocationSpooferService`.
    - [x] Define `BootReceiver`.
- [x] Initialize Hilt (`@HiltAndroidApp` Application class).

## Phase 2: Data Layer
- [x] Create Data Models.
    - [x] `HaStateResponse`
    - [x] `HaAttributes`
- [x] Create `HaApiService` interface (Retrofit).
    - [x] Define `GET /api/states/{entity_id}` endpoint.
- [x] Create `SettingsRepository`.
    - [x] Implement DataStore for `ha_base_url`, `target_entity_id`, `polling_interval`, `is_polling_enabled`, `is_spoofing_enabled`.
    - [x] Implement secure token storage for `ha_token` (EncryptedSharedPreferences)
- [x] Create Hilt Modules.
    - [x] `NetworkModule` (Provides Retrofit, OkHttp).
    - [x] `DataModule` (Provides Repository, DataStore).

## Phase 3: Service Layer (Core Logic)
+ [x] Create `LocationSpooferService`.
    - [x] Implement `onStartCommand` for Foreground Service promotion.
    - [x] Create Notification channel and persistent notification.
+ [x] Implement Polling Logic.
    - [x] Create a Coroutine loop that runs when `is_polling_enabled` is true.
    - [x] Call `HaApiService` to fetch location.
+ [x] Implement Mock Location Logic.
    - [x] Acquire `LocationManager`.
    - [x] Check for `ACCESS_MOCK_LOCATION` permission/capability (best-effort checks & error handling).
    - [x] Implement `setTestProviderLocation` to inject coordinates.
+ [x] Connect Service to Repository to observe settings changes.

## Phase 4: UI Layer
- [x] Create `MainViewModel`.
    - [x] Expose UI State (Service status, Last Location, Error messages).
    - [x] Functions to toggle polling/spoofing.
- [x] Build `SettingsScreen`.
    - [x] Input fields for HA URL, Token, Entity ID.
    - [x] Save buttons / Auto-save logic.
- [x] Build `DashboardScreen`.
    - [x] Status Header (Running/Stopped).
    - [x] Google Map View (centered on current/spoofed location).
    - [x] Control Toggles.
- [x] Set up Navigation Host.

## Phase 5: System Integration & Polish
- [x] Implement `BootReceiver`.
    - [x] Check `is_polling_enabled` preference.
    - [x] Start `LocationSpooferService` if enabled.
- [x] Handle Permissions.
    - [x] Request Runtime Permissions on app launch (Location & Notification request flows added).
    - [x] Check if App is selected as "Mock Location App" in Developer Options (ViewModel & PermissionChecker).
        - [x] Show dialog/intent to open Developer Options if not set.
- [x] Error Handling.
    ## Security & Token Storage
    - [x] Store HA token in Encrypted storage using EncryptedSharedPreferences (AndroidX Security Crypto)
    - [ ] Consider using Keystore-based protection and token rotation in future.

    - [x] Handle Network Errors (Retries, User Notification).
    - [x] Handle Invalid Token/Auth errors.
- [x] UI Polish (Material 3 styling).

## QA & CI
- [x] Add unit test dependencies and basic tests for data and UI.
- [x] Add CI workflow for build, test, and lint.
