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
- [ ] Create Data Models.
    - [ ] `HaStateResponse`
    - [ ] `HaAttributes`
- [ ] Create `HaApiService` interface (Retrofit).
    - [ ] Define `GET /api/states/{entity_id}` endpoint.
- [ ] Create `SettingsRepository`.
    - [ ] Implement DataStore for `ha_base_url`, `ha_token`, `target_entity_id`, `polling_interval`, `is_polling_enabled`, `is_spoofing_enabled`.
- [ ] Create Hilt Modules.
    - [ ] `NetworkModule` (Provides Retrofit, OkHttp).
    - [ ] `DataModule` (Provides Repository, DataStore).

## Phase 3: Service Layer (Core Logic)
- [ ] Create `LocationSpooferService`.
    - [ ] Implement `onStartCommand` for Foreground Service promotion.
    - [ ] Create Notification channel and persistent notification.
- [ ] Implement Polling Logic.
    - [ ] Create a Coroutine loop that runs when `is_polling_enabled` is true.
    - [ ] Call `HaApiService` to fetch location.
- [ ] Implement Mock Location Logic.
    - [ ] Acquire `LocationManager`.
    - [ ] Check for `ACCESS_MOCK_LOCATION` permission/capability.
    - [ ] Implement `setTestProviderLocation` to inject coordinates.
- [ ] Connect Service to Repository to observe settings changes.

## Phase 4: UI Layer
- [ ] Create `MainViewModel`.
    - [ ] Expose UI State (Service status, Last Location, Error messages).
    - [ ] Functions to toggle polling/spoofing.
- [ ] Build `SettingsScreen`.
    - [ ] Input fields for HA URL, Token, Entity ID.
    - [ ] Save buttons / Auto-save logic.
- [ ] Build `DashboardScreen`.
    - [ ] Status Header (Running/Stopped).
    - [ ] Google Map View (centered on current/spoofed location).
    - [ ] Control Toggles.
- [ ] Set up Navigation Host.

## Phase 5: System Integration & Polish
- [ ] Implement `BootReceiver`.
    - [ ] Check `is_polling_enabled` preference.
    - [ ] Start `LocationSpooferService` if enabled.
- [ ] Handle Permissions.
    - [ ] Request Runtime Permissions on app launch.
    - [ ] Check if App is selected as "Mock Location App" in Developer Options.
        - [ ] Show dialog/intent to open Developer Options if not set.
- [ ] Error Handling.
    - [ ] Handle Network Errors (Retries, User Notification).
    - [ ] Handle Invalid Token/Auth errors.
- [ ] UI Polish (Material 3 styling).
