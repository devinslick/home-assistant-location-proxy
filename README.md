# Home Assistant Location Proxy

An Android application that polls a specific location entity from your Home Assistant instance and "spoofs" your Android device's GPS location to match it.

## Why might you need this?

*   **Testing**: Useful for testing location-based automations, debugging presence detection, or mirroring the location of another device (like a car tracker) onto your phone.
*   **Improved privacy**: Decide when and how you share your location with apps.
*   **Battery life enhancements**: Run this on an emulator to share a spoofed location with certain apps (like Google Maps), reducing the number of apps that read location from the device in your pocket.

## Features

*   **Home Assistant Integration**: Connects directly to your Home Assistant API using a Long-Lived Access Token.
*   **Location Spoofing**: Injects mock GPS location data into the Android system, making other apps believe you are at the location reported by Home Assistant.
*   **Live Map View**: Visualizes the target location using OpenStreetMap (no Google API key required).
*   **Background Operation**: Runs as a Foreground Service to keep spoofing active even when the app is closed or the screen is off.
*   **Configurable Polling**: Adjust how often the app checks Home Assistant for updates.
*   **Android 16 Ready**: Tested and fully compatible with modern Android security and foreground service requirements.

## Prerequisites

*   **Android Device**: Running Android 7.0 (Nougat) or higher.
*   **Home Assistant**: A running instance accessible via network (HTTP/HTTPS).
*   **Entity ID**: The ID of the device tracker or person entity you want to mimic (e.g., `device_tracker.tesla_location` or `person.devin`).

## Installation & Setup

### 1. Install the App
Build the APK from source (see below) and install it on your device.

### 2. Enable Developer Options
To allow the app to control your GPS, you must enable Developer Options on your Android device:
1.  Go to **Settings** > **About Phone**.
2.  Tap **Build Number** 7 times until you see "You are now a developer!".

### 3. Select Mock Location App
1.  Go to **Settings** > **System** > **Developer Options**.
2.  Scroll down to the "Debugging" or "Location" section.
3.  Tap **Select mock location app**.
4.  Choose **HA Location Proxy**.

### 4. Grant Permissions
Open the app. You will be prompted to grant:
*   **Location Permission**: Required to inject location data.
*   **Notification Permission**: Required for the foreground service to run.

## Configuration

1.  **Base URL**: Enter your Home Assistant URL (e.g., `https://home-assistant.local:8123` or `https://my-ha-instance.com`).
2.  **Access Token**: Create a Long-Lived Access Token in Home Assistant:
    *   Go to your User Profile (bottom left in HA sidebar).
    *   Scroll to "Long-Lived Access Tokens".
    *   Click "Create Token", give it a name, and copy the string.
    *   Paste this token into the app settings.
3.  **Entity ID**: Enter the entity ID to track (e.g., `device_tracker.my_car`).
4.  **Polling Interval**: Set how frequently (in seconds) to update the location.

> **Security Note**: Your Access Token is stored securely on your device using Android's `EncryptedSharedPreferences` and is never sent anywhere except directly to your Home Assistant instance.

## Usage

1.  **Enable Polling**: Toggle the "Enable Polling" switch on the dashboard. The app will start fetching data from Home Assistant.
2.  **Enable Spoofing**: Toggle "Enable Spoofing". The app will now actively override your device's GPS location.
    *   *Note: This affects **all** apps on your device (e.g., Google Maps, Camera, Weather), not just this app.*
3.  **Map View**: The dashboard shows the current location reported by Home Assistant on an OpenStreetMap view.
4.  **Background**: You can minimize the app. A notification "HA Location Spoofer" will appear in the status bar, indicating the service is running.

## Troubleshooting

*   **App crashes immediately**: Ensure you have granted the **Notification Permission**. The foreground service cannot start without it.
*   **Location isn't changing**: Verify that **HA Location Proxy** is selected as the "Mock location app" in Android Developer Options.
*   **Connection Errors**:
    *   Ensure your Base URL includes `http://` or `https://`.
    *   If using a local URL (e.g., `homeassistant.local`), ensure your phone is on the same Wi-Fi network.
*   **"Incomplete location object" error**: This usually means the mock location data was missing a timestamp. Update to the latest version of the app where this is fixed.

## Development

### Tech Stack
*   **Language**: Kotlin
*   **UI**: Jetpack Compose (Material3)
*   **Architecture**: MVVM with Clean Architecture principles
*   **Dependency Injection**: Hilt
*   **Networking**: Retrofit + OkHttp
*   **Persistence**: Jetpack DataStore
*   **Maps**: osmdroid (OpenStreetMap)

### Building from Source

1.  Clone the repository.
2.  Open in Android Studio (Koala or newer recommended).
3.  Sync Gradle project.
4.  Run on a device or emulator.

**Note**: If building via command line on Windows:
```powershell
./gradlew assembleDebug
```
The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

