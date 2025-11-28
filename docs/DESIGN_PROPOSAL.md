# Technical Design Document: Home Assistant Location Spoofer

## 1. Project Overview

An Android application built with Kotlin and Jetpack Compose that polls a Home Assistant (HA) entity for location data (latitude/longitude) and injects that data into the Android Location Manager as a Mock Location.

## 2. Tech Stack & Architecture

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material3 / Material You Expressive)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Dependency Injection:** Hilt
*   **Network:** Retrofit + OkHttp (for HA API)
*   **Local Storage:** Jetpack DataStore (Preferences)
*   **Background Work:** Android Foreground Service (Required for continuous mocking)
*   **Maps:** Google Maps Compose library

## 3. Core Features & Logic

### 3.1. Configuration & Persistence

The app must persist the following settings using DataStore:

*   `ha_base_url` (String, e.g., "https://homeassistant.local:8123")
*   `ha_token` (String, Long-Lived Access Token)
*   `target_entity_id` (String, e.g., "device_tracker.my_car")
*   `polling_interval_seconds` (Long, Default: 30s)
*   `is_polling_enabled` (Boolean)
*   `is_spoofing_enabled` (Boolean)

**Import/Export Feature:**

*   **Import:** Ability to read a JSON file (URI selection) to populate the DataStore.
*   **Auto-Start:** If settings are valid upon app launch/boot, the service starts automatically.

### 3.2. The Spoofing Service (Foreground Service)

A Service class (`LocationSpooferService`) is required to keep the app alive.

*   **Trigger:** Starts on `BOOT_COMPLETED` (if enabled in config) or via UI toggle.
*   **Lifecycle:** Must run as a Foreground Service with a notification (e.g., "Spoofing location from HA...").
*   **Logic Loop:**
    1.  Check if `is_polling_enabled`.
    2.  Fetch state from HA API (`GET /api/states/<entity_id>`).
    3.  Extract `attributes.latitude`, `attributes.longitude`, and `attributes.altitude` (optional).
    4.  Update UI state (Last received location).
    5.  Check if `is_spoofing_enabled` AND MockLocationPermission is granted.
    6.  Inject location into `LocationManager.setTestProviderLocation`.
    7.  `delay(polling_interval)`

### 3.3. Permissions & Setup

The app must handle the following permissions:

*   `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`
*   `POST_NOTIFICATIONS` (Android 13+)
*   `RECEIVE_BOOT_COMPLETED`

**Special Case:** `ACCESS_MOCK_LOCATION`. The app must detect if it is selected as the mock provider. If not, show a dialog sending the user to Developer Options.

### 3.4. User Interface (Material You)

**Screen 1: Dashboard (Main Screen)**

*   **Header:** Status Indicator (Running/Stopped/Error).
*   **Map Card:** A Google Map composable centered on the last received coordinates. Includes a button to open Google Maps external intent.
*   **Controls:**
    *   Toggle: Enable Polling.
    *   Toggle: Enable Spoofing (Disabled if Polling is off).
*   **Logs/Status:** Text showing "Last update: 10:02 AM | Lat: 44.5, Lon: -99.2".

**Screen 2: Settings (BottomSheet or separate screen)**

*   Text Fields for HA URL, Token, Entity ID.
*   Slider/Input for Polling Interval.
*   "Backup/Restore" section (Export Config to JSON / Import Config from JSON).

## 4. API Data Model (Home Assistant)

We need to map the HA response.

```kotlin
data class HaStateResponse(
    val entity_id: String,
    val state: String,
    val attributes: HaAttributes,
    val last_updated: String
)

data class HaAttributes(
    val latitude: Double?,
    val longitude: Double?,
    val friendly_name: String?
)
```

## 5. Security & Safety

*   **Token Storage:** Tokens should be stored in EncryptedSharedPreferences (via DataStore).
*   **Fail-safe:** If HA returns a 401/404 or the network fails, the spoofing should pause, and the notification should update to "Connection Error" to prevent spoofing 0,0 coordinates.

## 6. Implementation Steps

1.  **Scaffold:** Create Compose Activity with Hilt setup.
2.  **Data Layer:** Create `HaApiService` interface and `SettingsRepository`.
3.  **Service Layer:** Implement `LocationSpooferService` with the polling coroutine.
4.  **Mock Logic:** Implement the `LocationManager` test provider logic.
5.  **UI:** Build the Material3 Dashboard.
6.  **Boot:** Implement `BootReceiver`.
