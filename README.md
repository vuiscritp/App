# Control Panel Terminal

Terminal-style Android control panel for **your own device**.

## Features
- Black background + white text terminal UI
- Command system (`android-help`, `android-info`, `android-list-apps`, ...)
- Flashlight, WiFi toggle (limited on Android 10+)
- Kill / open apps
- Accessibility Service support
- Many permissions requested

## How to build APK

### Method 1: Android Studio (Recommended)
1. Open Android Studio
2. File → Open → select the `ControlPanel` folder
3. Wait for Gradle sync
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. APK will be in `app/build/outputs/apk/debug/`

### Method 2: Command line
```bash
cd ControlPanel
./gradlew assembleDebug
```

## Important notes (No Root)

- **Force stop / clear cache** of other apps is heavily restricted. The app will open App Info page so you can do it manually.
- **WiFi toggle** on Android 10+ opens system panel instead of direct control.
- **Accessibility Service**, **Usage Access**, **Notification Listener**, **Overlay** must be enabled manually in Settings after install.
- Play Protect may warn because the app requests many sensitive permissions. This is expected for a powerful control panel.

## First run after install
1. Open the app
2. Allow all permissions it asks
3. Type `android-status`
4. Type `android-accessibility` → enable the service
5. Optionally enable Usage Access, Overlay, Notification access

## Commands
Type `android-help` inside the app.
