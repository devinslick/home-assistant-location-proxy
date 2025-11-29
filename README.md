# Home Assistant Location Proxy

This repository contains an Android app (Kotlin + Jetpack Compose) that polls a Home Assistant entity and mocks the Android location manager with those coordinates.

## Getting Started

Prerequisites:
- Android Studio Flamingo (or newer) with Kotlin support
- JDK 17

Build & Run:
- Open this folder as a project in Android Studio.
- Let Android Studio sync Gradle and download the dependencies.
- Run the `app` module on an Android emulator or device.

Build locally with Gradle wrapper (Windows):
1. Set `JAVA_HOME` (JDK 17) and `ANDROID_SDK_ROOT`.
2. If `gradle/wrapper/gradle-wrapper.jar` is not present, either install Gradle locally and run `gradle wrapper --gradle-version 8.2` to generate it, or call `gradle build` directly.
3. Run `./gradlew assembleDebug` (or `gradlew.bat assembleDebug` on Windows) and the debug APK will be at `app\build\outputs\apk\debug\app-debug.apk`.

Run tests locally:
- Unit tests: `./gradlew test` (or `gradlew.bat test` on Windows)
- Android lint: `./gradlew lint` (or `gradlew.bat lint` on Windows)
- CI workflow runs build, unit tests, and lint on push/PR via `.github/workflows/ci.yml`.

Gradle wrapper & CI notes:
- This repository currently does not commit `gradle/wrapper/gradle-wrapper.jar` to avoid repository bloat.
- CI uses `gradle/gradle-build-action@v3` to download and run a designated Gradle version (8.2), so pushes/PRs do not require the JAR in the repository.
- If you'd like to run `./gradlew` locally, you can either install Gradle locally and run `gradle wrapper --gradle-version 8.2` to generate the wrapper JAR, or run Gradle directly if installed:
	- Install Gradle and run: `gradle build` (which doesn't rely on wrapper)
	- Or use the `gradlew` script after generating the wrapper JAR in `gradle/wrapper`.

Notes:
- The project uses Hilt for dependency injection, Retrofit for networking, and Jetpack DataStore for local persistence.

## Phase 1 Completed
- Project skeleton created with Compose, Hilt, DataStore, Retrofit, and initial placeholders for the service and receiver.

