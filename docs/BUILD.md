# Build Instructions

This document outlines how to build an APK from this repository, both locally and using CI.

## Requirements
- Java 17 (OpenJDK 17). Ensure `JAVA_HOME` is set.
- Android SDK with Platform & Build-Tools for API 34 installed.
- Android platform tools (`adb`), emulator or connected device for `installDebug`.

## Recommended Windows Environment Setup
1. Install JDK 17 and set `JAVA_HOME`:

   ```powershell
   setx JAVA_HOME "C:\Program Files\Java\jdk-17" /M
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
   ```

2. Ensure Android SDK is configured (via Android Studio or SDK tools) and environment variables are set.

   ```powershell
   setx ANDROID_SDK_ROOT "C:\Users\<your-user>\AppData\Local\Android\Sdk" /M
   $env:ANDROID_SDK_ROOT = "C:\Users\<your-user>\AppData\Local\Android\Sdk"
   setx PATH "$Env:PATH;$env:ANDROID_SDK_ROOT\platform-tools" /M
   $env:PATH += ";$env:ANDROID_SDK_ROOT\platform-tools"
   ```

3. Install required SDKs and tools (command-line):

   ```powershell
   sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
   ```

## Build locally using Gradle wrapper (Windows PowerShell)
1. From the repository root, download dependencies and Gradle distribution using the wrapper:

   ```powershell
   .\gradlew.bat --version
   ```

2. Assemble the debug APK:

   ```powershell
   .\gradlew.bat assembleDebug
   ```

3. The APK will be located at:

   ```text
   app\build\outputs\apk\debug\app-debug.apk
   ```

4. (Optional) Install on a connected device:

   ```powershell
   .\gradlew.bat installDebug
   ```

## Build via GitHub Actions (no local setup required)
1. This repository includes a CI workflow that builds, runs tests, and uploads the debug APK as an artifact.
2. You can trigger a manual build by visiting the GitHub Actions tab and selecting the workflow called **CI**.
3. After the run completes, download the artifact named `app-debug-apk` from the workflow run.

## Release builds
- To produce a release APK/AAB, add a `signingConfigs` configuration and supply `keystore` credentials in `gradle.properties` or environment variables.
- Then run `.\gradlew.bat assembleRelease` to produce a release-signed APK (if properly configured).

## Troubleshooting
- `java` is not recognized: Install or set `JAVA_HOME` and ensure `java` is available in PATH.
- Gradle fails with missing Android SDK: Ensure `ANDROID_SDK_ROOT` is configured and the required Android SDK components are installed.
- If you prefer not to install Java/Android SDK locally, use CI and download the artifact from GitHub Actions.
