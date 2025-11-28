# Developer Notes: Mock Location and Developer Options

If the app needs to use `ACCESS_MOCK_LOCATION` for mocking Android locations, note that:

- Mock locations require the app to be selected as the "Mock Location App" in Developer Options. This is a developer-only setting and should be clearly communicated to users (e.g., in an onboarding flow or an in-app setting page) that this is not intended for production use.

How to set Mock Location App (Dev instructions):
1. Open Settings on your Android device.
2. Go to System > About phone.
3. Tap the Build number repeatedly until Developer Options are enabled.
4. Go back to Settings > System > Developer Options.
5. Find "Select mock location app" and choose the `Home Assistant Location Proxy` app from the list.

UX guidance for the app:
- If `ACCESS_MOCK_LOCATION` is not available (or not selected), show a prominent dialog explaining how to enable developer options and set the mock app.
- For non-technical users, include a `Learn more` link that opens a help page or a step-by-step guide.

Security & Privacy:
- Warn the user that mock locations can affect location-based apps and services.
- Always provide a clear way to disable spoofing and to revoke the mock location app setting.
- Ensure that any tokens or sensitive data are stored securely (EncryptedSharedPreferences / DataStore with an encrypted wrapper) and never logged in plaintext.

