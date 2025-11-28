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

Notes:
- The project uses Hilt for dependency injection, Retrofit for networking, and Jetpack DataStore for local persistence.

## Phase 1 Completed
- Project skeleton created with Compose, Hilt, DataStore, Retrofit, and initial placeholders for the service and receiver.

