# Copilot Instructions / Core Requirements

This document provides clear, compact instructions for an assistant (Copilot) when authoring, modifying, or extending code in this repository.

## Project identifiers
- Application package & namespace: `com.devinslick.homeassistantlocationproxy`
- App module: `:app`
- Minimum Android SDK: 24
- Target / Compile SDK: 34

## Architecture & Patterns
- Language: Kotlin (JVM target 17)
- UI: Jetpack Compose (Material 3)
- Architecture: MVVM with `ViewModel`s, `StateFlow`, and Jetpack Compose `Ui` state
- Dependency Injection: Hilt
- Networking: Retrofit + OkHttp
- Local persistence: Jetpack DataStore (Preferences), with secure storage for tokens (EncryptedSharedPreferences or encrypted-wrapped DataStore)
- Background service: Foreground Service using a `Service` class and persistent notification for location spoofing

## Keys & Config
- DataStore preference keys (use these constants in `SettingsRepository`):
  - `ha_base_url` (String) — Home Assistant base URL
  - `ha_token` (String) — Long-lived access token (store securely)
  - `target_entity_id` (String) — Entity to poll (e.g., `device_tracker.my_car`)
  - `polling_interval_seconds` (Long) — Polling interval (default 30s)
  - `is_polling_enabled` (Boolean)
  - `is_spoofing_enabled` (Boolean)

## Home Assistant API
- Endpoint to fetch entity state: `GET /api/states/{entity_id}`
- Expected JSON model mapping in code: `HaStateResponse` & `HaAttributes` with properties:
  - `entity_id`: String
  - `state`: String
  - `attributes.latitude`: Double?
  - `attributes.longitude`: Double?
  - `attributes.altitude`: Double? (optional)
  - `attributes.friendly_name`: String? (optional)

## Security & Token storage
- Tokens must be stored securely: prefer `EncryptedSharedPreferences` or an encrypted DataStore wrapper.
- Never log sensitive tokens or user credentials to logs or to crash reports unmasked or unencrypted.

## Permissions & UX
- Required Android permissions:
  - `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` (location accuracy)
  - `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION` (foreground location) where required
  - `POST_NOTIFICATIONS` (Android 13+)
  - `RECEIVE_BOOT_COMPLETED` (for auto-start)
- Special case: mock location requires developer options -> `Select mock location app` set to this app.
  - Provide a friendly UI flow to detect and direct users to enable developer options and select the mock app.
  - This is a developer-only flow and must be clearly marked.

## Mocking Location
- `LocationSpooferService` will:
  - Run as a Foreground Service with a notification showing status
  - Observe and honor `is_polling_enabled` and `is_spoofing_enabled` settings
  - Poll the HA API every `polling_interval_seconds` seconds when polling is enabled
  - Safely handle HA errors (401/404/network failure) by pausing spoofing and updating notification
  - Inject a mock location via `LocationManager.setTestProviderLocation` when spoofing is enabled and mock-location is allowed

## Coding Style & Implementation Notes
- Use coroutines and Kotlin Flows for reactive, non-blocking background work
- Use `try/catch` around network calls and map errors to UI-friendly states and notifications
- Hilt: use `NetworkModule` to provide `OkHttpClient` and `HaApiFactory`, `DataModule` to provide `DataStore` and repositories
- Keep services and repository interactions decoupled: `Service` observes flows from `SettingsRepository` or interacts with `NetworkRepository`
- Add unit tests for the Data Layer and simple UI tests for the Compose screens

## Files of note
- `app/src/main/kotlin/com/devinslick/homeassistantlocationproxy` — main app package
- `app/src/main/java/com/devinslick/homeassistantlocationproxy/data/SettingsRepository.kt` — DataStore wrapper
- `app/src/main/java/com/devinslick/homeassistantlocationproxy/network/HaApiService.kt`, `HaApiFactory.kt`
- `app/src/main/java/com/devinslick/homeassistantlocationproxy/service/LocationSpooferService.kt`
- `app/src/main/java/com/devinslick/homeassistantlocationproxy/di/NetworkModule.kt`, `DataModule.kt`

## Development workflow & expectations for Copilot
- Always follow project conventions: MVVM, Hilt DI, Kotlin Flow and Compose.
- Be explicit about permissions and when to request them at runtime.
- For every new feature:
  1. Update the `DESIGN_PROPOSAL.md` if the feature is architectural.
  2. Add implementation TODOs to `docs/TASK_LIST.md` as needed.
  3. Add or update tests (unit for data, instrumentation for services) and update docs with any new user-facing behavior.

## Package name reminder
- Use `com.devinslick.homeassistantlocationproxy` as the package name in code and `applicationId` in Gradle.
 - CI uses `gradle/gradle-build-action` and the repository does not commit `gradle-wrapper.jar` to avoid repository bloat.
 - When contributing: use `gradle` locally or generate a wrapper if you prefer to use `./gradlew` locally; CI will run using the specified Gradle version.

